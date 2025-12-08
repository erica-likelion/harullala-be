package likelion.harullala.infra;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import likelion.harullala.domain.Character;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatGptClient {
    
    @Value("${openai.api.key:}")
    private String apiKey;
    
    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;
    
    @Value("${openai.api.model:gpt-3.5-turbo}")
    private String model;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public String generateFeedback(String emotionText, Character character) {
        log.info("=== AI 피드백 생성 시작 ===");
        log.info("emotionText 길이: {}, character: {}", emotionText != null ? emotionText.length() : 0, 
                character != null ? character.getName() : "null");
        
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("API 키가 없습니다. 기본 응답 반환");
            return "오늘의 감정 피드백: " + emotionText;
        }
        
        try {
            String prompt = buildCharacterPrompt(emotionText, character);
            log.debug("프롬프트 생성 완료. 프롬프트 길이: {}", prompt.length());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                    Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 200,
                "temperature", 0.7
            );
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            log.info("OpenAI API 호출 시작: {}", apiUrl);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
            
            log.info("API 응답 상태: {}", response.getStatusCode());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                log.debug("API 응답 본문 존재");
                
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                log.info("Choices 개수: {}", choices != null ? choices.size() : 0);
                
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) message.get("content");
                    log.info("=== 원본 응답 (처리 전) ===");
                    log.info("{}", content);
                    
                    String cleaned = cleanResponse(content);
                    log.info("=== 정리된 응답 (처리 후) ===");
                    log.info("{}", cleaned);
                    log.info("=== AI 피드백 생성 완료 ===");
                    return cleaned;
                } else {
                    log.warn("Choices가 비어있습니다. 기본 응답 반환");
                }
            } else {
                log.error("API 호출 실패. 상태: {}, 본문 존재: {}", response.getStatusCode(), response.getBody() != null);
            }
            
            // API 호출 실패 시 기본 응답
            log.warn("기본 응답 반환: 오늘의 감정 피드백");
            return "오늘의 감정 피드백: " + emotionText;
            
        } catch (Exception e) {
            // 예외 발생 시 기본 응답
            log.error("예외 발생: {} - {}", e.getClass().getName(), e.getMessage(), e);
            return "오늘의 감정 피드백: " + emotionText;
        }
    }
    
    private String buildCharacterPrompt(String emotionText, Character character) {
        // Character 엔티티의 정보 직접 사용 (null 체크 제거 - 모든 유저는 캐릭터 선택함)
        String characterDescription = character.getDescription();
        String characterTag = character.getTag();
        String characterName = character.getName();
        
        // 캐릭터별 말투 지정
        String speechStyle = getCharacterSpeechStyle(characterName);
        
        return String.format("""
            You are a character named "%s" with personality trait "%s".
            Detailed personality: %s
            
            User's emotion record today: %s
            
            CRITICAL - Character Personality:
            - You MUST embody the "%s" personality trait in EVERY response
            - Express the unique characteristics described in: "%s"
            - DO NOT use generic or neutral tone - be DISTINCTLY this character
            - Let this character's personality shine through STRONGLY
            
            Speech Style:
            - %s
            - You MUST use this speech style consistently in EVERY response
            
            Response Rules:
            - Stay in character as "%s" (%s personality)
            - Write 3-4 sentences (not shorter, not longer)
            - Use the specified speech style: %s
            - Respond in Korean naturally matching this character's speaking style
            - NO quotation marks, NO emojis, NO markdown formatting
            - Pure text only
            
            Respond to the user's emotion as this character would.
            """, characterName, characterTag, characterDescription, emotionText, 
                 characterTag, characterDescription, speechStyle,
                 characterName, characterTag, speechStyle);
    }
    
    /**
     * 캐릭터별 말투 반환
     */
    private String getCharacterSpeechStyle(String characterName) {
        if (characterName == null) {
            return "존댓말";
        }
        
        return switch (characterName) {
            case "츠츠", "티티", "파파" -> "반말";
            case "루루" -> "존댓말";
            case "동동" -> "반존대: 같은 문장 안에서 반말과 존댓말을 섞어 사용. 예: '너 요즘 너무 무리하는 거 아니에요?', '잠깐 쉬어가도 돼요', '오늘은 좀 일찍 자거나요'. 반말 어미(-어, -아, -지)와 존댓말 어미(-요, -네요, -에요)를 자연스럽게 혼합";
            default -> "존댓말";
        };
    }
    
    /**
     * 감정 리포트용 캐릭터 멘트 생성
     * @param reportSummary 리포트 요약 정보 (이번 달 통계)
     * @param character 캐릭터 정보
     * @return AI 생성 캐릭터 멘트
     */
    public String generateReportMessage(String reportSummary, Character character) {
        if (apiKey == null || apiKey.isEmpty()) {
            // API 키가 없으면 기본 응답 반환
            return "이번 달도 수고했어요! 계속 응원할게요!";
        }
        
        try {
            String prompt = buildReportPrompt(reportSummary, character);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                    Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 200,
                "temperature", 0.7
            );
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) message.get("content");
                    return cleanResponse(content); // 따옴표, 이모티콘, 마크다운 제거
                }
            }
            
            // API 호출 실패 시 기본 응답
            return "이번 달도 수고했어요! 계속 응원할게요!";
            
        } catch (Exception e) {
            // 예외 발생 시 기본 응답
            return "이번 달도 수고했어요! 계속 응원할게요!";
        }
    }
    
    /**
     * 커스텀 프롬프트로 AI 메시지 생성
     */
    public String generateCustomFeedback(String customPrompt) {
        log.info("=== 커스텀 AI 피드백 생성 시작 ===");
        log.info("프롬프트 길이: {}", customPrompt != null ? customPrompt.length() : 0);
        
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("API 키가 없습니다.");
            return "AI 서비스를 사용할 수 없습니다.";
        }
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                    Map.of("role", "user", "content", customPrompt)
                ),
                "max_tokens", 200,
                "temperature", 0.7
            );
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            log.info("OpenAI API 호출 시작: {}", apiUrl);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
            
            log.info("API 응답 상태: {}", response.getStatusCode());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                log.debug("API 응답 본문 존재");
                
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                log.info("Choices 개수: {}", choices != null ? choices.size() : 0);
                
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) message.get("content");
                    log.info("=== 원본 응답 (처리 전) ===");
                    log.info("{}", content);
                    
                    String cleaned = cleanResponse(content);
                    log.info("=== 정리된 응답 (처리 후) ===");
                    log.info("{}", cleaned);
                    log.info("=== 커스텀 AI 피드백 생성 완료 ===");
                    return cleaned;
                } else {
                    log.warn("Choices가 비어있습니다. 기본 응답 반환");
                }
            } else {
                log.error("API 호출 실패. 상태: {}", response.getStatusCode());
            }
            
            // API 호출 실패 시 기본 응답
            log.warn("기본 응답 반환: 이번 달도 수고했어요");
            return "이번 달도 수고했어요! 계속 응원할게요!";
            
        } catch (Exception e) {
            // 예외 발생 시 기본 응답
            log.error("예외 발생: {} - {}", e.getClass().getName(), e.getMessage(), e);
            return "이번 달도 수고했어요! 계속 응원할게요!";
        }
    }
    
    /**
     * AI 응답에서 따옴표, 이모티콘, 마크다운 제거
     */
    private String cleanResponse(String response) {
        if (response == null) {
            log.warn("응답이 null입니다.");
            return "";
        }
        
        log.debug("정리 전: {}", response);
        
        // 따옴표 제거 (큰따옴표, 작은따옴표)
        String cleaned = response.replaceAll("[\"']", "");
        
        // 마크다운 제거 (**굵게**, *기울임*, __밑줄__ 등)
        cleaned = cleaned.replaceAll("\\*\\*([^*]+)\\*\\*", "$1"); // **굵게** -> 굵게
        cleaned = cleaned.replaceAll("\\*([^*]+)\\*", "$1"); // *기울임* -> 기울임
        cleaned = cleaned.replaceAll("__([^_]+)__", "$1"); // __밑줄__ -> 밑줄
        cleaned = cleaned.replaceAll("_([^_]+)_", "$1"); // _기울임_ -> 기울임
        
        // 이모티콘 제거 (유니코드 이모티콘 범위)
        cleaned = cleaned.replaceAll("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+", "");
        
        // 앞뒤 공백 제거
        cleaned = cleaned.trim();
        
        log.debug("정리 후: {}", cleaned);
        
        return cleaned;
    }
    
    /**
     * 감정 리포트용 프롬프트 생성
     */
    private String buildReportPrompt(String reportSummary, Character character) {
        String characterDescription = character.getDescription() != null 
            ? character.getDescription() 
            : "사용자의 감정을 공감하는 상담사입니다.";
        
        String characterTag = character.getTag() != null 
            ? character.getTag() 
            : "일반적";
        
        String characterName = character.getName() != null 
            ? character.getName() 
            : "상담사";
        
        // 캐릭터별 말투 지정
        String speechStyle = getCharacterSpeechStyle(characterName);
        
        return String.format("""
            You are a character named "%s" with personality trait "%s".
            Detailed personality: %s
            
            User's monthly emotion report:
            %s
            
            CRITICAL - Character Personality:
            - You MUST embody the "%s" personality trait in your encouragement
            - Express the unique characteristics described in: "%s"
            - DO NOT use generic or neutral tone - be DISTINCTLY this character
            - Let this character's personality shine through STRONGLY
            
            Speech Style:
            - %s
            - You MUST use this speech style consistently in EVERY response
            
            Response Rules:
            - Stay in character as "%s" (%s personality)
            - 1-2 sentences maximum
            - Encourage user based on their monthly emotion patterns
            - Use the specified speech style: %s
            - Respond in Korean naturally matching this character's speaking style
            - NO quotation marks, NO emojis, NO markdown formatting
            - Pure text only
            
            Give encouraging message as this character would.
            """, characterName, characterTag, characterDescription, reportSummary, 
                 characterTag, characterDescription, speechStyle,
                 characterName, characterTag, speechStyle);
    }
}
