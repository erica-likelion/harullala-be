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
        
        String prompt = String.format("""
            You are a character named "%s" with personality trait "%s".
            Detailed personality: %s
            
            Current situation: The user is on the friend invite page. (A screen where they can copy their unique code to share with friends)
            
            CRITICAL - Character Personality:
            - You MUST embody the "%s" personality trait in EVERY response
            - Express the unique characteristics described in: "%s"
            - DO NOT use generic or neutral tone - be DISTINCTLY this character
            - Let this character's personality shine through STRONGLY
            
            Response Rules:
            - Stay in character as "%s" (%s personality)
            - 1 line maximum, very short (10-15 characters in Korean)
            - Encourage the user to invite friends and record emotions together
            - Don't be pushy, be natural and in character
            - Use natural Korean matching this character's speaking style
            - NO quotation marks, NO emojis, NO markdown formatting
            - Pure text only
            
            Tell the user in Korean that it would be nice to record emotions with friends, using this character's speaking style.
            """, characterName, characterTag, characterDescription, 
                 characterTag, characterDescription, characterName, characterTag);
        
        return chatGptClient.generateCustomFeedback(prompt);
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

