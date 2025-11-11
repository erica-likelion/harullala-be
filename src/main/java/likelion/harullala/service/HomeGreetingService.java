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
        
        // 3. 캐시 키 생성 (userId + 날짜 + 기록여부)
        String cacheKey = createCacheKey(userId, hasRecordedToday);
        
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
     * 캐시 키 생성 (userId + 날짜 + 기록여부)
     * 날짜를 포함하여 자정이 지나면 자동으로 새로운 키 생성
     */
    private String createCacheKey(Long userId, boolean hasRecorded) {
        String today = LocalDateTime.now().toLocalDate().toString(); // yyyy-MM-dd
        return userId + "_" + today + "_" + hasRecorded;
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
        
        String prompt;
        
        if (hasRecordedToday) {
            // 오늘 이미 기록했을 때
            prompt = String.format("""
                당신은 '%s'(%s) 캐릭터입니다.
                캐릭터 성격: %s
                
                현재 상황: 사용자가 오늘 이미 감정 기록을 작성했습니다.
                
                사용자에게 오늘 이미 기록을 완료했다는 것을 캐릭터의 말투와 성격에 맞게 한 줄로 짧게 알려주세요.
                
                규칙:
                - 반드시 한 줄로 짧게 (15-25자)
                - 위 캐릭터의 성격과 말투를 정확히 반영
                - "오늘 이미 기록했다"는 내용을 자연스럽게 전달
                - 캐릭터의 개성이 드러나는 표현 사용
                """, characterName, characterTag, characterDescription);
        } else {
            // 아직 기록 안 했을 때
            prompt = String.format("""
                당신은 '%s'(%s) 캐릭터입니다.
                캐릭터 성격: %s
                
                현재 상황: 사용자가 아직 오늘 감정 기록을 작성하지 않았습니다.
                
                사용자에게 오늘 기분을 물어보거나 감정 기록을 유도하는 캐릭터의 말투로 한 줄 짧게 인사해주세요.
                
                규칙:
                - 반드시 한 줄로 짧게 (10-15자)
                - 위 캐릭터의 성격과 말투를 정확히 반영
                - 기분을 묻거나 기록을 가볍게 유도
                - 강요하지 말고 캐릭터답게 자연스럽게
                """, characterName, characterTag, characterDescription);
        }
        
        return chatGptClient.generateCustomFeedback(prompt);
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

