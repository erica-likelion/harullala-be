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
import likelion.harullala.dto.CharacterInfo;
import likelion.harullala.dto.ReminderResponse;
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
    public ReminderResponse generateReminder(Long userId) {
        // 1. 친구 목록 조회
        List<Long> friendIds = getFriendIds(userId);
        int totalFriends = friendIds.size();
        
        if (totalFriends == 0) {
            // 친구가 없으면 기본 메시지
            return ReminderResponse.builder()
                    .message("아직 친구가 없네! 친구를 추가해서 함께 기록해봐~")
                    .totalFriends(0)
                    .recordedFriends(0)
                    .character(null)
                    .hasUnrecorded(false)
                    .build();
        }
        
        // 2. 오늘 기록 작성한 친구 수 조회
        int recordedFriends = countTodayRecordedFriends(friendIds);
        boolean hasUnrecorded = recordedFriends < totalFriends;
        
        // 3. 현재 캐릭터 조회
        Character character = getCurrentCharacter(userId);
        
        // 4. 캐시 키 생성
        String cacheKey = createCacheKey(userId, totalFriends, recordedFriends);
        
        // 5. 캐시 확인
        CachedMessage cached = cache.get(cacheKey);
        String message;
        
        if (cached != null && !cached.isExpired()) {
            message = cached.getMessage();
        } else {
            // 6. AI로 메시지 생성
            message = generateWithAI(character, totalFriends, recordedFriends, hasUnrecorded);
            
            // 7. 캐시 저장
            cache.put(cacheKey, new CachedMessage(message, LocalDateTime.now()));
        }
        
        // 8. 응답 생성
        return ReminderResponse.builder()
                .message(message)
                .totalFriends(totalFriends)
                .recordedFriends(recordedFriends)
                .character(character != null ? CharacterInfo.from(character) : null)
                .hasUnrecorded(hasUnrecorded)
                .build();
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
                .map(UserCharacter::getCharacter)
                .orElse(null);
    }
    
    /**
     * 캐시 키 생성
     */
    private String createCacheKey(Long userId, int friendCount, int recordedCount) {
        return userId + "_" + friendCount + "_" + recordedCount;
    }
    
    /**
     * AI로 메시지 생성
     */
    private String generateWithAI(Character character, int friendCount, int recordedCount, boolean hasUnrecorded) {
        String characterName = character != null ? character.getName() : "상담사";
        String characterDescription = character != null && character.getDescription() != null
                ? character.getDescription()
                : "친절하고 공감적인 상담사";
        
        String prompt;
        
        if (friendCount == 0) {
            // 친구가 없을 때
            prompt = String.format("""
                당신은 '%s' 캐릭터입니다.
                캐릭터 성격: %s
                
                사용자에게 아직 친구가 없다는 것을 알려주고, 친구를 초대하면 더 재미있다는 것을 캐릭터의 말투로 한 줄로 격려하는 메시지를 작성해주세요.
                (예: "아직 친구가 없네! 친구를 초대해서 함께 기록하면 더 재밌을 거야~")
                """, characterName, characterDescription);
        } else if (hasUnrecorded) {
            // 친구가 아직 기록 안 했을 때
            int unrecordedCount = friendCount - recordedCount;
            prompt = String.format("""
                당신은 '%s' 캐릭터입니다.
                캐릭터 성격: %s
                
                현재 상황: 친구 %d명 중 %d명이 아직 오늘 감정 기록을 작성하지 않았습니다.
                
                위 상황을 사용자에게 자연스럽게 알려주고, 친구들이 기록을 꼬박 쓰도록 격려하는 캐릭터의 말투로 한 줄 메시지를 작성해주세요.
                
                올바른 예시:
                - "친구들 중 아직 기록 안 한 사람이 있네! 꼬박 쓰라고 응원해줘~"
                - "몇 명이 아직 기록을 안 썼어! 우리 친구들 응원할까?"
                - "친구들 기록 상태를 보니 아직 안 쓴 사람들이 있구나~ 격려해봐!"
                
                주의: "너가 친구들에게 쓰라고 해줘"가 아니라, "친구들이 아직 안 썼다는 상황을 알려주고 격려"하는 톤으로 작성해주세요.
                """, characterName, characterDescription, friendCount, unrecordedCount);
        } else {
            // 모두 기록했을 때
            prompt = String.format("""
                당신은 '%s' 캐릭터입니다.
                캐릭터 성격: %s
                
                현재 상황: 친구 %d명이 모두 오늘 감정 기록을 작성했습니다!
                
                위 상황을 축하하며 격려하는 캐릭터의 말투로 한 줄 메시지를 작성해주세요.
                (예: "모두 기록 잘했네! 정말 대단해!")
                """, characterName, characterDescription, friendCount);
        }
        
        return chatGptClient.generateCustomFeedback(prompt);
    }
}

