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

import likelion.harullala.domain.AiCharacter;
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
    
    public String generateFeedback(String emotionText, String emoji, AiCharacter aiCharacter) {
        if (apiKey == null || apiKey.isEmpty()) {
            // API 키가 없으면 기본 응답 반환
            return "오늘의 감정(" + emoji + ") 피드백: " + emotionText;
        }
        
        try {
            String prompt = buildCharacterPrompt(emotionText, emoji, aiCharacter);
            
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
    
    private String buildCharacterPrompt(String emotionText, String emoji, AiCharacter aiCharacter) {
        // AiCharacter enum의 description 사용
        String characterDescription = aiCharacter.getDescription();
        
        return String.format("""
            당신은 '%s' 캐릭터입니다.
            캐릭터 성격: %s
            
            사용자가 다음과 같은 감정을 기록했습니다:
            감정: %s
            내용: %s
            
            이 감정 기록에 대해 위의 성격에 맞는 피드백을 한국어로 제공해주세요. 
            사용자의 감정을 인정하고, 앞으로의 마음가짐에 도움이 되는 조언을 해주세요.
            피드백은 2-3문장 정도로 간결하게 작성해주세요.
            """, aiCharacter.getCode(), characterDescription, emoji, emotionText);
    }
    
    // 기존 메서드 유지 (하위 호환성)
    public String generateFeedback(String emotionText, String emoji) {
        return generateFeedback(emotionText, emoji, AiCharacter.F); // 기본값: F 캐릭터
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
            
            return "메시지를 생성할 수 없습니다.";
            
        } catch (Exception e) {
            return "AI 서비스 오류가 발생했습니다.";
        }
    }
}