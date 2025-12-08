package likelion.harullala.service;

import com.google.firebase.messaging.*;
import likelion.harullala.domain.Notification;
import likelion.harullala.domain.NotificationType;
import likelion.harullala.domain.User;
import likelion.harullala.dto.NotificationListResponse;
import likelion.harullala.dto.NotificationResponse;
import likelion.harullala.dto.UnreadCountResponse;
import likelion.harullala.repository.NotificationRepository;
import likelion.harullala.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 푸시 알림 서비스
 * FCM을 통한 실시간 알림 전송 및 알림 내역 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 알림 전송 (DB 저장 + FCM 푸시)
     */
    @Transactional
    public void sendNotification(Long userId, NotificationType type, String title, String message, Long relatedId) {
        sendNotification(userId, type, title, message, relatedId, false);
    }

    /**
     * 알림 전송 (DB 저장 + FCM 푸시)
     * @param skipPush true이면 FCM 푸시를 보내지 않음 (DB에는 저장됨)
     */
    @Transactional
    public void sendNotification(Long userId, NotificationType type, String title, String message, Long relatedId, boolean skipPush) {
        log.info("알림 전송 요청: userId={}, type={}, title={}, skipPush={}", userId, type, title, skipPush);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        log.info("사용자 조회 완료: userId={}, fcmToken 존재={}", userId, user.getFcmToken() != null && !user.getFcmToken().isEmpty());

        // 1. DB에 알림 저장
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .relatedId(relatedId)
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);
        log.info("알림 저장 완료: userId={}, type={}, title={}, notificationId={}, skipPush={}", 
                userId, type, title, notification.getId(), skipPush);

        // 2. FCM 푸시 전송 (skipPush가 false일 때만)
        if (!skipPush && user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
            log.info("FCM 전송 시도: userId={}, fcmToken={}", userId, 
                    user.getFcmToken().length() > 20 ? user.getFcmToken().substring(0, 20) + "..." : user.getFcmToken());
            sendFcmNotification(user.getFcmToken(), type, title, message, relatedId, notification.getId());
        } else if (skipPush) {
            log.info("푸시 알림이 차단되어 FCM 전송을 건너뜁니다. userId={}", userId);
        } else {
            log.warn("FCM 토큰이 없습니다. userId={}, fcmToken={}", userId, user.getFcmToken());
        }
    }

    /**
     * FCM 푸시 알림 전송
     */
    private void sendFcmNotification(String fcmToken, NotificationType type, String title, String message, Long relatedId, Long notificationId) {
        try {
            log.info("FCM 전송 시도: type={}, title={}, fcmToken={}", type, title, 
                fcmToken != null && fcmToken.length() > 20 ? fcmToken.substring(0, 20) + "..." : fcmToken);
            
            // 알림 데이터 구성
            Map<String, String> data = new HashMap<>();
            data.put("type", type.name());
            data.put("title", title);
            data.put("message", message);
            data.put("notificationId", String.valueOf(notificationId));
            if (relatedId != null) {
                data.put("relatedId", String.valueOf(relatedId));
            }

            // FCM 메시지 생성
            Message fcmMessage = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(message)
                            .build())
                    .putAllData(data)
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setSound("default")
                                    .build())
                            .build())
                    .build();

            // FCM 전송
            log.info("FirebaseMessaging.getInstance() 호출 전");
            String response = FirebaseMessaging.getInstance().send(fcmMessage);
            log.info("FCM 전송 성공: response={}", response);

        } catch (FirebaseMessagingException e) {
            log.error("FCM 전송 실패 (FirebaseMessagingException): errorCode={}, message={}", 
                    e.getMessagingErrorCode(), e.getMessage());
            e.printStackTrace();
            
            // 토큰이 유효하지 않은 경우 처리
            if (e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT ||
                e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                log.warn("유효하지 않은 FCM 토큰입니다. 토큰 삭제를 고려해야 합니다.");
            }
        } catch (IllegalStateException e) {
            log.error("FCM 전송 실패 (IllegalStateException): Firebase가 초기화되지 않았습니다. message={}", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            log.error("알 수 없는 오류로 FCM 전송 실패: exception={}, message={}", e.getClass().getName(), e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 사용자의 알림 목록 조회 (페이징 + 날짜 필터)
     * @param days null이면 전체, 7이면 최근 7일, 30이면 최근 30일
     */
    public NotificationListResponse getNotifications(Long userId, int page, int size, Integer days) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage;
        
        if (days != null && days > 0) {
            // 특정 기간 내 알림 조회
            java.time.LocalDateTime fromDate = java.time.LocalDateTime.now().minusDays(days);
            notificationPage = notificationRepository.findByUserAndCreatedAtAfterOrderByCreatedAtDesc(
                user, fromDate, pageable);
        } else {
            // 전체 알림 조회
            notificationPage = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        }

        List<NotificationResponse> notifications = notificationPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return NotificationListResponse.builder()
                .notifications(notifications)
                .currentPage(notificationPage.getNumber())
                .totalPages(notificationPage.getTotalPages())
                .totalElements(notificationPage.getTotalElements())
                .hasNext(notificationPage.hasNext())
                .build();
    }

    /**
     * 안읽은 알림 개수 조회
     */
    public UnreadCountResponse getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        long unreadCount = notificationRepository.countByUserAndIsReadFalse(user);
        return new UnreadCountResponse(unreadCount);
    }

    /**
     * 특정 알림 읽음 처리 (읽은 시간 기록)
     */
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 알림에 접근할 권한이 없습니다.");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    /**
     * 모든 알림 읽음 처리
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalse(user);
        
        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
        }
        
        notificationRepository.saveAll(unreadNotifications);
        log.info("모든 알림 읽음 처리 완료: userId={}, count={}", userId, unreadNotifications.size());
    }

    /**
     * FCM 토큰 등록
     */
    @Transactional
    public void registerFcmToken(Long userId, String fcmToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.updateFcmToken(fcmToken);
        userRepository.save(user);
        log.info("FCM 토큰 등록 완료: userId={}", userId);
    }

    /**
     * Notification -> NotificationResponse 변환
     */
    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedId(notification.getRelatedId())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

