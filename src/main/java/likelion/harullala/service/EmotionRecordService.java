package likelion.harullala.service;

import likelion.harullala.domain.EmotionRecord;
import likelion.harullala.domain.FriendRelationship;
import likelion.harullala.domain.NotificationType;
import likelion.harullala.domain.User;
import likelion.harullala.dto.EmotionCreateRequest;
import likelion.harullala.dto.EmotionDeleteResponse;
import likelion.harullala.dto.EmotionListResponse;
import likelion.harullala.dto.EmotionResponse;
import likelion.harullala.dto.EmotionUpdateRequest;
import likelion.harullala.dto.EmotionUpdateResponse;
import likelion.harullala.exception.EmotionRecordNotFoundException;
import likelion.harullala.exception.ForbiddenAccessException;
import likelion.harullala.repository.EmotionRecordRepository;
import likelion.harullala.repository.FriendNotificationBlockRepository;
import likelion.harullala.repository.FriendRelationshipRepository;
import likelion.harullala.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmotionRecordService {

    private final EmotionRecordRepository emotionRecordRepository;
    private final FriendRelationshipRepository friendRelationshipRepository;
    private final FriendNotificationBlockRepository friendNotificationBlockRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public EmotionResponse createEmotionRecord(Long userId, EmotionCreateRequest request) {
        // 감정 기록 엔티티 생성 (색상, 감정명 포함)
        EmotionRecord emotionRecord = EmotionRecord.builder()
                .userId(userId)
                .record(request.getRecord())
                .emotionName(request.getEmotion_name())
                .mainColor(request.getMain_color())
                .subColor(request.getSub_color())
                .textColor(request.getText_color()) // 텍스트 색상 추가
                .isShared(request.getIs_shared()) // 사용자가 선택한 공유 여부
                .aiFeedbackCount(request.getAi_feedback_count() != null ? request.getAi_feedback_count() : 0) // AI 피드백 횟수 (기본값 0)
                .build();

        // 저장
        EmotionRecord savedRecord = emotionRecordRepository.save(emotionRecord);

        // 친구들에게 알림 발송
        try {
            sendFriendNotifications(userId, savedRecord.getRecordId());
        } catch (Exception e) {
            // 알림 전송 실패해도 감정 기록 생성은 정상 처리
        }

        // Response로 변환하여 반환
        return EmotionResponse.from(savedRecord);
    }

    /**
     * 친구들에게 감정 기록 알림 발송
     */
    private void sendFriendNotifications(Long userId, Long recordId) {
        // 사용자 정보 조회
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }

        // 친구 목록 조회
        List<FriendRelationship> friendships = friendRelationshipRepository.findAcceptedFriendsByUserId(userId);

        // 각 친구에게 알림 발송 (차단되지 않은 친구만)
        for (FriendRelationship friendship : friendships) {
            Long friendId = friendship.getOtherUserId(userId);
            
            // 친구가 나를 차단했는지 확인
            User friend = userRepository.findById(friendId).orElse(null);
            if (friend == null) {
                continue;
            }
            
            // 차단 여부 확인
            if (friendNotificationBlockRepository.existsByUserAndBlockedFriend(friend, user)) {
                // 이 친구가 나를 차단했으므로 알림을 보내지 않음
                continue;
            }
            
            try {
                notificationService.sendNotification(
                    friendId,
                    NotificationType.FRIEND_EMOTION_RECORD,
                    "친구가 감정 기록을 작성했어요",
                    user.getNickname() + "님이 오늘의 감정을 기록했어요",
                    recordId
                );
            } catch (Exception e) {
                // 개별 알림 실패는 로그만 남기고 계속 진행
            }
        }
    }

    /**
     * 감정기록 목록 조회 (페이지네이션)
     */
    public List<EmotionListResponse> getEmotionRecordList(Long userId, int page, int size) {
        // 페이지는 0부터 시작하므로 -1
        Pageable pageable = PageRequest.of(page - 1, size);
        
        // 사용자의 감정기록 조회 (최신순)
        Page<EmotionRecord> emotionRecords = emotionRecordRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        // DTO로 변환
        return emotionRecords.getContent().stream()
                .map(EmotionListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 감정기록 단일 조회
     */
    public EmotionResponse getEmotionRecord(Long userId, Long recordId) {
        // 감정기록 조회
        EmotionRecord emotionRecord = emotionRecordRepository.findById(recordId)
                .orElseThrow(() -> new EmotionRecordNotFoundException("Emotion record not found"));

        // 권한 확인 - 본인의 감정기록인지 체크
        if (!emotionRecord.getUserId().equals(userId)) {
            throw new ForbiddenAccessException("You do not have permission");
        }

        // Response로 변환하여 반환
        return EmotionResponse.from(emotionRecord);
    }

    /**
     * 감정기록 수정
     */
    @Transactional
    public EmotionUpdateResponse updateEmotionRecord(Long userId, Long recordId, EmotionUpdateRequest request) {
        // 감정기록 조회
        EmotionRecord emotionRecord = emotionRecordRepository.findById(recordId)
                .orElseThrow(() -> new EmotionRecordNotFoundException("Emotion record not found"));

        // 권한 확인 - 본인의 감정기록인지 체크
        if (!emotionRecord.getUserId().equals(userId)) {
            throw new ForbiddenAccessException("You do not have permission to update this record");
        }

        // 감정기록 전체 업데이트 (더티 체킹으로 자동 업데이트)
        emotionRecord.update(
                request.getRecord(),
                request.getEmotion_name(),
                request.getMain_color(),
                request.getSub_color(),
                request.getText_color(),
                request.getAi_feedback_count()
        );

        // 공유 상태도 함께 업데이트
        if (request.getIs_shared() != null) {
            emotionRecord.updateSharedStatus(request.getIs_shared());
        }

        // Response로 변환하여 반환
        return EmotionUpdateResponse.from(emotionRecord);
    }

    /**
     * 감정기록 삭제
     */
    @Transactional
    public EmotionDeleteResponse deleteEmotionRecord(Long userId, Long recordId) {
        // 감정기록 조회
        EmotionRecord emotionRecord = emotionRecordRepository.findById(recordId)
                .orElseThrow(() -> new EmotionRecordNotFoundException("Emotion record not found"));

        // 권한 확인 - 본인의 감정기록인지 체크
        if (!emotionRecord.getUserId().equals(userId)) {
            throw new ForbiddenAccessException("You do not have permission");
        }

        // 실제 삭제 (Hard Delete)
        emotionRecordRepository.delete(emotionRecord);

        // Response로 변환하여 반환
        return EmotionDeleteResponse.of(recordId);
    }

    /**
     * 감정기록 공유 상태 변경
     */
    @Transactional
    public EmotionResponse updateSharedStatus(Long userId, Long recordId, Boolean isShared) {
        // 감정기록 조회
        EmotionRecord emotionRecord = emotionRecordRepository.findById(recordId)
                .orElseThrow(() -> new EmotionRecordNotFoundException("Emotion record not found"));

        // 권한 확인 - 본인의 감정기록인지 체크
        if (!emotionRecord.getUserId().equals(userId)) {
            throw new ForbiddenAccessException("You do not have permission");
        }

        // 공유 상태 변경 (더티 체킹으로 자동 업데이트)
        emotionRecord.updateSharedStatus(isShared);

        // Response로 변환하여 반환
        return EmotionResponse.from(emotionRecord);
    }

    /**
     * 공유된 감정기록 목록 조회 (페이지네이션)
     */
    public List<EmotionListResponse> getSharedEmotionRecordList(int page, int size) {
        // 페이지는 0부터 시작하므로 -1
        Pageable pageable = PageRequest.of(page - 1, size);
        
        // 공유된 감정기록 조회 (최신순)
        Page<EmotionRecord> emotionRecords = emotionRecordRepository
                .findByIsSharedTrueOrderByCreatedAtDesc(pageable);
        
        // DTO로 변환
        return emotionRecords.getContent().stream()
                .map(EmotionListResponse::from)
                .collect(Collectors.toList());
    }
}


