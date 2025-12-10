package likelion.harullala.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import likelion.harullala.domain.Character;
import likelion.harullala.domain.UserCharacter;
import likelion.harullala.dto.HomeGreetingResponse;
import likelion.harullala.infra.ChatGptClient;
import likelion.harullala.repository.EmotionRecordRepository;
import likelion.harullala.repository.UserCharacterRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 홈 화면 AI 인사말 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeGreetingService {
    
    private final ChatGptClient chatGptClient;
    private final EmotionRecordRepository emotionRecordRepo;
    private final UserCharacterRepository userCharacterRepo;
    
    private final Map<String, CachedGreeting> cache = new ConcurrentHashMap<>();
    
    /**
     * 홈 화면 AI 인사말 생성
     * - 하루에 최대 2번만 AI 호출 (기록 안 했을 때 1번, 기록 후 1번)
     * - 캐시는 다음날 00:00까지 유효
     */
    public HomeGreetingResponse generateGreeting(Long userId) {
        // 1. 오늘 기록 작성 여부 확인
        boolean hasRecordedToday = checkTodayRecord(userId);
        
        // 2. 현재 캐릭터 조회
        Character character = getCurrentCharacter(userId);
        String characterName = character != null ? character.getName() : "피코";
        Long characterId = character != null ? character.getId() : null;
        
        // 3. 캐시 키 생성 (userId + 캐릭터ID + 날짜 + 기록여부)
        String cacheKey = createCacheKey(userId, characterId, hasRecordedToday);
        
        // 4. 캐시 확인 (오늘 자정까지 유효)
        CachedGreeting cached = cache.get(cacheKey);
        String message;
        
        if (cached != null && !cached.isExpired()) {
            message = cached.getMessage();
        } else {
            // 5. AI로 메시지 생성 (하루에 최대 2번만 호출)
            message = generateWithAI(character, hasRecordedToday);
            
            // 6. 캐시 저장 (오늘 자정까지 유효)
            LocalDateTime endOfDay = LocalDateTime.now()
                    .withHour(23)
                    .withMinute(59)
                    .withSecond(59);
            cache.put(cacheKey, new CachedGreeting(message, endOfDay));
        }
        
        // 7. 응답 반환
        return HomeGreetingResponse.builder()
                .message(message)
                .hasRecordedToday(hasRecordedToday)
                .characterName(characterName)
                .build();
    }
    
    /**
     * 오늘 감정 기록 작성 여부 확인
     */
    private boolean checkTodayRecord(Long userId) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        long count = emotionRecordRepo.countByUserIdAndDateRange(userId, startOfDay, endOfDay);
        return count > 0;
    }
    
    /**
     * 현재 캐릭터 조회
     */
    private Character getCurrentCharacter(Long userId) {
        return userCharacterRepo.findByUserId(userId)
                .map(UserCharacter::getSelectedCharacter)
                .orElse(null);
    }
    
    /**
     * 캐시 키 생성 (userId + 캐릭터ID + 날짜 + 기록여부)
     * 캐릭터 변경 시에도 새로 생성되도록 캐릭터 ID 포함
     * 날짜를 포함하여 자정이 지나면 자동으로 새로운 키 생성
     */
    private String createCacheKey(Long userId, Long characterId, boolean hasRecorded) {
        String today = LocalDateTime.now().toLocalDate().toString(); // yyyy-MM-dd
        return userId + "_" + (characterId != null ? characterId : "null") + "_" + today + "_" + hasRecorded;
    }
    
    /**
     * AI로 인사말 생성
     */
    private String generateWithAI(Character character, boolean hasRecordedToday) {
        String characterName = character != null ? character.getName() : "피코";
        String characterTag = character != null && character.getTag() != null 
                ? character.getTag() 
                : "";
        String characterDescription = character != null && character.getDescription() != null
                ? character.getDescription()
                : "친근하고 다정한 친구";
        
        // 캐릭터별 말투 지정
        String speechStyle = getCharacterSpeechStyle(characterName);
        
        String prompt;
        
        if (hasRecordedToday) {
            // 오늘 이미 기록했을 때
            prompt = String.format("""
                You are a character named "%s" with personality trait "%s".
                Detailed personality: %s
                
                Current situation: The user has already written their emotion record today.
                
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
                - 1 line maximum, very short (10-15 characters in Korean)
                - Inform the user that they have already completed today's record
                - Use the specified speech style: %s
                - NO quotation marks, NO emojis, NO markdown formatting
                - Pure text only
                
                Korean Language Quality:
                - 반드시 자연스러운 한국어로 응답하세요
                - 번역투 표현을 절대 사용하지 마세요 (예: "~것입니다", "쓰는 감정기록", "쓰기 기록" 등 어색한 표현 금지)
                - 한국인이 일상에서 실제로 사용하는 표현만 사용하세요
                - 어색하거나 기계적인 번역체 문장은 절대 금지
                
                Respond in Korean naturally matching this character's speaking style.
                """, characterName, characterTag, characterDescription, 
                     characterTag, characterDescription, speechStyle,
                     characterName, characterTag, speechStyle);
        } else {
            // 아직 기록 안 했을 때
            prompt = String.format("""
                You are a character named "%s" with personality trait "%s".
                Detailed personality: %s
                
                Current situation: The user has not yet written their emotion record today.
                
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
                - 1 line maximum, very short (10-15 characters in Korean)
                - Ask about their mood or gently encourage them to write a record
                - Don't be pushy, be natural and in character
                - Use the specified speech style: %s
                - NO quotation marks, NO emojis, NO markdown formatting
                - Pure text only
                
                Greet the user in Korean naturally matching this character's speaking style.
                """, characterName, characterTag, characterDescription, 
                     characterTag, characterDescription, speechStyle,
                     characterName, characterTag, speechStyle);
        }
        
        return chatGptClient.generateCustomFeedback(prompt);
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
     * 캐시된 인사말
     * - 오늘 자정까지 유효 (하루에 최대 2번만 AI 호출)
     */
    @Getter
    @AllArgsConstructor
    private static class CachedGreeting {
        private final String message;
        private final LocalDateTime expireAt; // 만료 시간 (오늘 23:59:59)
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expireAt);
        }
    }
}

