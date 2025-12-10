package likelion.harullala.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import likelion.harullala.domain.Character;
import likelion.harullala.domain.UserCharacter;
import likelion.harullala.dto.FriendInviteGreetingResponse;
import likelion.harullala.infra.ChatGptClient;
import likelion.harullala.repository.UserCharacterRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 친구 초대 페이지 AI 멘트 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendInviteGreetingService {
    
    private final ChatGptClient chatGptClient;
    private final UserCharacterRepository userCharacterRepo;
    
    private final Map<String, CachedGreeting> cache = new ConcurrentHashMap<>();
    
    /**
     * 친구 초대 페이지 AI 멘트 생성
     * - 하루에 1번만 AI 호출 (친구 초대 상황은 하나뿐)
     * - 캐시는 오늘 자정까지 유효
     */
    public FriendInviteGreetingResponse generateInviteGreeting(Long userId) {
        // 1. 현재 캐릭터 조회
        Character character = getCurrentCharacter(userId);
        String characterName = character != null ? character.getName() : "피코";
        Long characterId = character != null ? character.getId() : null;
        
        // 2. 캐시 키 생성 (userId + 캐릭터ID + 날짜)
        String cacheKey = createCacheKey(userId, characterId);
        
        // 3. 캐시 확인 (오늘 자정까지 유효)
        CachedGreeting cached = cache.get(cacheKey);
        String message;
        
        if (cached != null && !cached.isExpired()) {
            message = cached.getMessage();
        } else {
            // 4. AI로 메시지 생성 (하루에 최대 1번만 호출)
            message = generateWithAI(character);
            
            // 5. 캐시 저장 (오늘 자정까지 유효)
            LocalDateTime endOfDay = LocalDateTime.now()
                    .withHour(23)
                    .withMinute(59)
                    .withSecond(59);
            cache.put(cacheKey, new CachedGreeting(message, endOfDay));
        }
        
        // 6. 응답 반환
        return FriendInviteGreetingResponse.builder()
                .message(message)
                .characterName(characterName)
                .build();
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
     * 캐시 키 생성 (userId + 캐릭터ID + 날짜)
     * 캐릭터 변경 시에도 새로 생성되도록 캐릭터 ID 포함
     */
    private String createCacheKey(Long userId, Long characterId) {
        String today = LocalDateTime.now().toLocalDate().toString(); // yyyy-MM-dd
        return "invite_" + userId + "_" + (characterId != null ? characterId : "null") + "_" + today;
    }
    
    /**
     * AI로 친구 초대 멘트 생성
     */
    private String generateWithAI(Character character) {
        String characterName = character != null ? character.getName() : "피코";
        String characterTag = character != null && character.getTag() != null 
                ? character.getTag() 
                : "";
        String characterDescription = character != null && character.getDescription() != null
                ? character.getDescription()
                : "친근하고 다정한 친구";
        
        // 캐릭터별 말투 지정
        String speechStyle = getCharacterSpeechStyle(characterName);
        
        String prompt = String.format("""
            You are a character named "%s" with personality trait "%s".
            Detailed personality: %s
            
            Current situation: The user is on the friend invite page. (A screen where they can copy their unique code to share with friends)
            
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
            - Encourage the user to invite friends to write records together
            - Don't be pushy, be natural and in character
            - Use the specified speech style: %s
            - NO quotation marks, NO emojis, NO markdown formatting
            - Pure text only
            
            Korean Language Quality:
            - 반드시 자연스러운 한국어로 응답하세요
            - 번역투 표현을 절대 사용하지 마세요 (예: "~것입니다", "쓰는 감정기록", "쓰기 기록" 등 어색한 표현 금지)
            - 한국인이 일상에서 실제로 사용하는 표현만 사용하세요
            - 어색하거나 기계적인 번역체 문장은 절대 금지
            
            Tell the user in Korean that it would be nice to write records with friends, using this character's speaking style.
            """, characterName, characterTag, characterDescription, 
                 characterTag, characterDescription, speechStyle,
                 characterName, characterTag, speechStyle);
        
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
     * 캐시된 멘트
     * - 오늘 자정까지 유효 (하루에 최대 1번만 AI 호출)
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

