package likelion.harullala.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import likelion.harullala.domain.CachedMessage;
import likelion.harullala.domain.Character;
import likelion.harullala.domain.FriendRelationship;
import likelion.harullala.domain.UserCharacter;
import likelion.harullala.infra.ChatGptClient;
import likelion.harullala.repository.EmotionRecordRepository;
import likelion.harullala.repository.FriendRelationshipRepository;
import likelion.harullala.repository.UserCharacterRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendReminderService {
    
    private final ChatGptClient chatGptClient;
    private final FriendRelationshipRepository friendRepo;
    private final EmotionRecordRepository emotionRecordRepo;
    private final UserCharacterRepository userCharacterRepo;
    
    private final Map<String, CachedMessage> cache = new ConcurrentHashMap<>();
    
    /**
     * 친구 기록 리마인드 메시지 생성
     */
    public String generateReminder(Long userId) {
        // 1. 친구 목록 조회
        List<Long> friendIds = getFriendIds(userId);
        int totalFriends = friendIds.size();
        
        // 2. 오늘 기록 작성한 친구 수 조회
        int recordedFriends = countTodayRecordedFriends(friendIds);
        boolean hasUnrecorded = recordedFriends < totalFriends;
        
        // 3. 현재 캐릭터 조회
        Character character = getCurrentCharacter(userId);
        Long characterId = character != null ? character.getId() : null;
        
        // 4. 캐시 키 생성 (캐릭터 변경 시에도 새로 생성되도록 캐릭터 ID 포함)
        String cacheKey = createCacheKey(userId, characterId, totalFriends, recordedFriends);
        
        // 5. 캐시 확인
        CachedMessage cached = cache.get(cacheKey);
        String message;
        
        if (cached != null && !cached.isExpired()) {
            message = cached.getMessage();
        } else {
            // 6. AI로 메시지 생성 (친구가 없을 때도 캐릭터에 맞게 생성)
            message = generateWithAI(character, totalFriends, recordedFriends, hasUnrecorded);
            
            // 7. 캐시 저장
            cache.put(cacheKey, new CachedMessage(message, LocalDateTime.now()));
        }
        
        // 8. 메시지 반환
        return message;
    }
    
    /**
     * 사용자의 친구 ID 목록 조회
     */
    private List<Long> getFriendIds(Long userId) {
        List<FriendRelationship> relationships = friendRepo.findAcceptedFriendsByUserId(userId);
        
        return relationships.stream()
                .map(relationship -> relationship.getOtherUserId(userId))
                .collect(Collectors.toList());
    }
    
    /**
     * 오늘 기록 작성한 친구 수 조회
     */
    private int countTodayRecordedFriends(List<Long> friendIds) {
        if (friendIds.isEmpty()) {
            return 0;
        }
        
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        long count = 0;
        for (Long friendId : friendIds) {
            long friendRecordCount = emotionRecordRepo.countByUserIdAndDateRange(friendId, startOfDay, endOfDay);
            if (friendRecordCount > 0) {
                count++;
            }
        }
        
        return (int) count;
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
     * 캐시 키 생성
     * 캐릭터 변경, 친구 수 변경, 기록한 친구 수 변경 시 새로 생성
     */
    private String createCacheKey(Long userId, Long characterId, int friendCount, int recordedCount) {
        return userId + "_" + (characterId != null ? characterId : "null") + "_" + friendCount + "_" + recordedCount;
    }
    
    /**
     * AI로 메시지 생성
     */
    private String generateWithAI(Character character, int friendCount, int recordedCount, boolean hasUnrecorded) {
        String characterName = character != null ? character.getName() : "상담사";
        String characterTag = character != null && character.getTag() != null
                ? character.getTag()
                : "";
        String characterDescription = character != null && character.getDescription() != null
                ? character.getDescription()
                : "현재 상황을 알려주는 알리미";
        
        String prompt;
        
        if (friendCount == 0) {
            // 친구가 없을 때
            prompt = String.format("""
                You are a character named "%s" with personality trait "%s".
                Detailed personality: %s
                
                Current situation: The user has no friends yet.
                
                CRITICAL - Character Personality:
                - You MUST embody the "%s" personality trait in EVERY response
                - Express the unique characteristics described in: "%s"
                - DO NOT use generic or neutral tone - be DISTINCTLY this character
                - Let this character's personality shine through STRONGLY
                
                Response Rules:
                - Stay in character as "%s" (%s personality)
                - 1 line maximum, very short
                - Inform the user they have no friends yet
                - Encourage them to invite Pico World friends (up to 5 friends can be invited)
                - Use natural Korean matching this character's speaking style
                - NO quotation marks, NO emojis, NO markdown formatting
                - Pure text only
                
                Tell the user in Korean that they can invite friends to make recording more fun.
                """, characterName, characterTag, characterDescription, 
                     characterTag, characterDescription, characterName, characterTag);
        } else if (hasUnrecorded) {
            // 친구가 아직 기록 안 했을 때
            int unrecordedCount = friendCount - recordedCount;
            prompt = String.format("""
                You are a character named "%s" with personality trait "%s".
                Detailed personality: %s
                
                Current situation: Out of %d friends, %d friends have not yet written their emotion record today.
                
                CRITICAL - Character Personality:
                - You MUST embody the "%s" personality trait in EVERY response
                - Express the unique characteristics described in: "%s"
                - DO NOT use generic or neutral tone - be DISTINCTLY this character
                - Let this character's personality shine through STRONGLY
                
                Response Rules:
                - Stay in character as "%s" (%s personality)
                - 1 line maximum, very short
                - Inform the user about friends who haven't recorded yet
                - Encourage friends to write records regularly
                - Tone: Inform about the situation, not asking user to tell friends
                - Use natural Korean matching this character's speaking style
                - NO quotation marks, NO emojis, NO markdown formatting
                - Pure text only
                
                Inform the user in Korean about friends who haven't recorded yet, using this character's speaking style.
                """, characterName, characterTag, characterDescription, friendCount, unrecordedCount,
                     characterTag, characterDescription, characterName, characterTag);
        } else {
            // 모두 기록했을 때
            prompt = String.format("""
                You are a character named "%s" with personality trait "%s".
                Detailed personality: %s
                
                Current situation: All %d friends have written their emotion record today!
                
                CRITICAL - Character Personality:
                - You MUST embody the "%s" personality trait in EVERY response
                - Express the unique characteristics described in: "%s"
                - DO NOT use generic or neutral tone - be DISTINCTLY this character
                - Let this character's personality shine through STRONGLY
                
                Response Rules:
                - Stay in character as "%s" (%s personality)
                - 1 line maximum, very short
                - Celebrate that all friends have recorded
                - Use natural Korean matching this character's speaking style
                - NO quotation marks, NO emojis, NO markdown formatting
                - Pure text only
                
                Celebrate in Korean that all friends have recorded, using this character's speaking style.
                """, characterName, characterTag, characterDescription, friendCount,
                     characterTag, characterDescription, characterName, characterTag);
        }
        
        return chatGptClient.generateCustomFeedback(prompt);
    }
}

