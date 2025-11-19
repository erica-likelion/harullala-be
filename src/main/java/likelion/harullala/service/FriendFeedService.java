package likelion.harullala.service;

import likelion.harullala.domain.EmotionRecord;
import likelion.harullala.domain.FeedReadStatus;
import likelion.harullala.domain.FriendRelationship;
import likelion.harullala.domain.User;
import likelion.harullala.dto.FriendFeedResponse;
import likelion.harullala.dto.MarkFeedReadRequest;
import likelion.harullala.exception.EmotionRecordNotFoundException;
import likelion.harullala.exception.ForbiddenAccessException;
import likelion.harullala.repository.EmotionRecordRepository;
import likelion.harullala.repository.FeedReadStatusRepository;
import likelion.harullala.repository.FriendRelationshipRepository;
import likelion.harullala.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendFeedService {

    private final EmotionRecordRepository emotionRecordRepository;
    private final FeedReadStatusRepository feedReadStatusRepository;
    private final FriendRelationshipRepository friendRelationshipRepository;
    private final UserRepository userRepository;

    /**
     * 친구들의 공유된 피드 조회 (24시간 이내)
     */
    public List<FriendFeedResponse> getFriendFeeds(Long userId, int page, int size) {
        // 페이지는 0부터 시작하므로 -1
        Pageable pageable = PageRequest.of(page - 1, size);
        
        // 사용자의 친구 목록 조회 (ACCEPTED 상태만)
        List<Long> friendIds = getFriendIds(userId);
        
        if (friendIds.isEmpty()) {
            return List.of(); // 친구가 없으면 빈 리스트 반환
        }

        // 24시간 이내의 공유된 감정 기록 조회
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        
        // 모든 공유된 기록을 조회한 후 Java에서 필터링
        List<EmotionRecord> allSharedRecords = emotionRecordRepository.findAll()
                .stream()
                .filter(record -> record.getIsShared() && 
                                 friendIds.contains(record.getUserId()) && 
                                 record.getCreatedAt().isAfter(twentyFourHoursAgo))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
        
        // 페이지네이션 적용
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allSharedRecords.size());
        List<EmotionRecord> sharedRecords = allSharedRecords.subList(start, end);

        // 사용자가 읽은 기록 ID 목록 조회
        List<Long> readRecordIds = feedReadStatusRepository.findReadRecordIdsByReaderId(userId);

        // DTO로 변환
        return sharedRecords.stream()
                .map(record -> {
                    User author = userRepository.findById(record.getUserId())
                            .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다."));
                    
                    boolean isRead = readRecordIds.contains(record.getRecordId());
                    long readCount = feedReadStatusRepository.countByEmotionRecord(record);
                    
                    return FriendFeedResponse.from(record, author.getNickname(), author.getProfileImageUrl(), isRead, readCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * 친구 피드 상세 조회 (권한 검사 포함)
     */
    public FriendFeedResponse getFriendFeedDetail(Long userId, Long recordId) {
        // 감정 기록 조회
        EmotionRecord record = emotionRecordRepository.findById(recordId)
                .orElseThrow(() -> new EmotionRecordNotFoundException("감정 기록을 찾을 수 없습니다."));

        // 공유된 기록인지 확인
        if (!record.getIsShared()) {
            throw new ForbiddenAccessException("공유되지 않은 기록입니다.");
        }

        // 친구 관계 확인
        if (!isFriend(userId, record.getUserId())) {
            throw new ForbiddenAccessException("친구의 기록만 조회할 수 있습니다.");
        }

        // 작성자 정보 조회
        User author = userRepository.findById(record.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다."));

        // 현재 사용자 조회
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 읽음 상태 확인
        boolean isRead = feedReadStatusRepository.existsByReaderAndEmotionRecord(currentUser, record);
        long readCount = feedReadStatusRepository.countByEmotionRecord(record);

        return FriendFeedResponse.from(record, author.getNickname(), author.getProfileImageUrl(), isRead, readCount);
    }

    /**
     * 피드 읽음 처리
     */
    @Transactional
    public void markFeedAsRead(Long userId, MarkFeedReadRequest request) {
        // 감정 기록 조회
        EmotionRecord record = emotionRecordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new EmotionRecordNotFoundException("감정 기록을 찾을 수 없습니다."));

        // 공유된 기록인지 확인
        if (!record.getIsShared()) {
            throw new ForbiddenAccessException("공유되지 않은 기록입니다.");
        }

        // 친구 관계 확인
        if (!isFriend(userId, record.getUserId())) {
            throw new ForbiddenAccessException("친구의 기록만 읽을 수 있습니다.");
        }

        // 현재 사용자 조회
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이미 읽었는지 확인
        if (feedReadStatusRepository.existsByReaderAndEmotionRecord(currentUser, record)) {
            return; // 이미 읽었으면 아무것도 하지 않음
        }

        // 읽음 상태 저장
        FeedReadStatus readStatus = FeedReadStatus.builder()
                .reader(currentUser)
                .emotionRecord(record)
                .build();

        feedReadStatusRepository.save(readStatus);
    }

    /**
     * 사용자의 친구 ID 목록 조회 (ACCEPTED 상태만)
     */
    private List<Long> getFriendIds(Long userId) {
        List<FriendRelationship> relationships = friendRelationshipRepository.findAcceptedFriendsByUserId(userId);
        
        return relationships.stream()
                .map(relationship -> relationship.getOtherUserId(userId))
                .collect(Collectors.toList());
    }

    /**
     * 두 사용자가 친구인지 확인 (ACCEPTED 상태만)
     */
    private boolean isFriend(Long userId1, Long userId2) {
        return friendRelationshipRepository.existsByUsersAndAccepted(
                userRepository.findById(userId1).orElseThrow(),
                userRepository.findById(userId2).orElseThrow()
        );
    }
}
