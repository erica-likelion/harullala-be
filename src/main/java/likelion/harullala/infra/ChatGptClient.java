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
        // Character 테이블의 description과 tag를 활용한 프롬프트 생성
        String characterDescription = character.getDescription() != null 
            ? character.getDescription() 
            : "사용자의 감정을 공감하는 상담사입니다.";
        
        String characterTag = character.getTag() != null 
            ? character.getTag() 
            : "일반적";
        
        // 캐릭터 이름을 프롬프트에 포함 (더 개인화된 느낌)
        String characterName = character.getName() != null 
            ? character.getName() 
            : "상담사";
        
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
}