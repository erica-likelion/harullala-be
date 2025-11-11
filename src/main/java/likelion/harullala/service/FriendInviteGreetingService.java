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
        
        // 2. 캐시 키 생성 (userId + 날짜)
        String cacheKey = createCacheKey(userId);
        
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
     * 캐시 키 생성 (userId + 날짜)
     */
    private String createCacheKey(Long userId) {
        String today = LocalDateTime.now().toLocalDate().toString(); // yyyy-MM-dd
        return "invite_" + userId + "_" + today;
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
            당신은 '%s'(%s) 캐릭터입니다.
            캐릭터 성격: %s
            
            현재 상황: 사용자가 친구 초대 페이지에 있습니다. (자신의 고유 코드를 복사해서 친구에게 공유하는 화면)
            
            사용자에게 친구와 함께 감정 기록을 하면 좋다는 것을 캐릭터의 말투로 짧게 한 줄로 말해주세요.
            
            규칙:
            - 반드시 한 줄로 짧게 (10-15자)
            - 위 캐릭터의 성격과 말투를 정확히 반영
            - 친구와 함께 기록하는 것을 유도하는 톤
            - 강요하지 말고 캐릭터답게 자연스럽게
            """, characterName, characterTag, characterDescription);
        
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

