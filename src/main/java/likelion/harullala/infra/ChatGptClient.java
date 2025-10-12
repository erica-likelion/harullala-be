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
        String characterPrompt = switch (aiCharacter) {
            case T -> "당신은 논리적이고 분석적인 성격의 상담사입니다. 감정보다는 객관적이고 논리적인 관점에서 조언해주세요.";
            case F -> "당신은 감정적이고 공감적인 성격의 상담사입니다. 사용자의 감정에 깊이 공감하고 따뜻한 마음으로 조언해주세요.";
            case EMOTIONAL -> "당신은 감정을 풍부하게 표현하는 상담사입니다. 사용자의 감정을 함께 느끼고 감정적으로 소통해주세요.";
            case COOL -> "당신은 냉철하고 객관적인 성격의 상담사입니다. 감정에 치우치지 않고 현실적이고 실용적인 조언을 해주세요.";
        };
        
        return String.format("""
            %s
            
            사용자가 다음과 같은 감정을 기록했습니다:
            감정: %s
            내용: %s
            
            이 감정 기록에 대해 위의 성격에 맞는 피드백을 한국어로 제공해주세요. 
            사용자의 감정을 인정하고, 앞으로의 마음가짐에 도움이 되는 조언을 해주세요.
            피드백은 2-3문장 정도로 간결하게 작성해주세요.
            """, characterPrompt, emoji, emotionText);
    }
    
    // 기존 메서드 유지 (하위 호환성)
    public String generateFeedback(String emotionText, String emoji) {
        return generateFeedback(emotionText, emoji, AiCharacter.F); // 기본값: F 캐릭터
    }
}