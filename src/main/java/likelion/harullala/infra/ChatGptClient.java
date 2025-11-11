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
    
    public String generateFeedback(String emotionText, String emoji, Character character) {
        if (apiKey == null || apiKey.isEmpty()) {
            // API 키가 없으면 기본 응답 반환
            return "오늘의 감정(" + emoji + ") 피드백: " + emotionText;
        }
        
        try {
            String prompt = buildCharacterPrompt(emotionText, emoji, character);
            
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
                    return (String) message.get("content");
                }
            }
            
            // API 호출 실패 시 기본 응답
            return "오늘의 감정(" + emoji + ") 피드백: " + emotionText;
            
        } catch (Exception e) {
            // 예외 발생 시 기본 응답
            return "오늘의 감정(" + emoji + ") 피드백: " + emotionText;
        }
    }
    
    private String buildCharacterPrompt(String emotionText, String emoji, Character character) {
        // Character 엔티티의 정보 직접 사용 (null 체크 제거 - 모든 유저는 캐릭터 선택함)
        String characterDescription = character.getDescription();
        String characterTag = character.getTag();
        String characterName = character.getName();
        
        return String.format("""
            당신은 %s(%s) 캐릭터입니다.
            캐릭터 성격: %s
            
            사용자의 오늘 감정 기록:
            감정: %s
            내용: %s
            
            위 캐릭터의 말투와 성격에 맞게 짧고 자연스럽게 반응해주세요.
            장황하지 않게, 캐릭터가 직접 말하는 것처럼 대화 형식으로 답변해주세요.
            
            규칙:
            - 캐릭터의 개성 있는 말투 사용
            - 2-3문장 이내로 간결하게
            - 감정을 인정하고 응원하는 톤
            - 진정성 있고 따뜻한 느낌
            """, characterName, characterTag, characterDescription, emoji, emotionText);
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
                    return (String) message.get("content");
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
        if (apiKey == null || apiKey.isEmpty()) {
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
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
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
        
        return String.format("""
            당신은 %s(%s) 캐릭터입니다.
            캐릭터 성격: %s
            
            사용자의 이번 달 감정 리포트:
            %s
            
            위 리포트를 바탕으로, 캐릭터의 말투와 성격에 맞게 사용자를 응원하고 격려하는 멘트를 해주세요.
            
            규칙:
            - 캐릭터의 개성 있는 말투 사용
            - 3-4문장 이내로 간결하게
            - 사용자의 이번 달 감정 패턴을 언급하면서 응원
            - 긍정적이고 따뜻한 톤
            - 진정성 있고 공감하는 느낌
            - 다음 달도 함께하자는 격려
            """, characterName, characterTag, characterDescription, reportSummary);
    }
}
