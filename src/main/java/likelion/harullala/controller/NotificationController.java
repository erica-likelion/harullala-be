package likelion.harullala.controller;

import jakarta.validation.Valid;
import likelion.harullala.dto.*;
import likelion.harullala.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 푸시 알림 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * FCM 토큰 등록
     * POST /api/v1/notifications/token
     */
    @PostMapping("/token")
    public ResponseEntity<ApiSuccess<Void>> registerFcmToken(
            @Valid @RequestBody FcmTokenRequest request) {
        Long userId = getCurrentUserId();
        notificationService.registerFcmToken(userId, request.getFcmToken());
        
        return ResponseEntity.ok(ApiSuccess.of(null, "FCM 토큰이 등록되었습니다."));
    }

    /**
     * 알림 목록 조회 (페이징 + 날짜 필터)
     * GET /api/v1/notifications?page=0&size=20&days=7
     */
    @GetMapping
    public ResponseEntity<ApiSuccess<NotificationListResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer days) {
        Long userId = getCurrentUserId();
        NotificationListResponse response = notificationService.getNotifications(userId, page, size, days);
        
        return ResponseEntity.ok(ApiSuccess.of(response, "알림 목록을 조회했습니다."));
    }

    /**
     * 안읽은 알림 개수 조회
     * GET /api/v1/notifications/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiSuccess<UnreadCountResponse>> getUnreadCount() {
        Long userId = getCurrentUserId();
        UnreadCountResponse response = notificationService.getUnreadCount(userId);
        
        return ResponseEntity.ok(ApiSuccess.of(response, "안읽은 알림 개수를 조회했습니다."));
    }

    /**
     * 특정 알림 읽음 처리
     * PUT /api/v1/notifications/{notificationId}/read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiSuccess<Void>> markAsRead(@PathVariable Long notificationId) {
        Long userId = getCurrentUserId();
        notificationService.markAsRead(userId, notificationId);
        
        return ResponseEntity.ok(ApiSuccess.of(null, "알림을 읽음 처리했습니다."));
    }

    /**
     * 현재 인증된 사용자 ID 가져오기
     * TODO: 실제 인증 시스템과 연동 필요
     */
    private Long getCurrentUserId() {
        // 임시로 하드코딩 (실제로는 SecurityContext에서 가져와야 함)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
                && !authentication.getPrincipal().equals("anonymousUser")) {
            // 실제 구현에서는 CustomUserDetails에서 userId를 가져옴
            return 1L; // 임시
        }
        return 1L; // 임시
    }
}

