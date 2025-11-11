package likelion.harullala.controller;

import jakarta.validation.Valid;
import likelion.harullala.dto.*;
import likelion.harullala.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import likelion.harullala.config.security.CustomUserDetails;

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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FcmTokenRequest request) {
        Long userId = userDetails.getUser().getId();
        notificationService.registerFcmToken(userId, request.getFcmToken());
        
        return ResponseEntity.ok(ApiSuccess.of(null, "FCM 토큰이 등록되었습니다."));
    }

    /**
     * 알림 목록 조회 (페이징 + 날짜 필터)
     * GET /api/v1/notifications?page=0&size=20&days=7
     */
    @GetMapping
    public ResponseEntity<ApiSuccess<NotificationListResponse>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer days) {
        Long userId = userDetails.getUser().getId();
        NotificationListResponse response = notificationService.getNotifications(userId, page, size, days);
        
        return ResponseEntity.ok(ApiSuccess.of(response, "알림 목록을 조회했습니다."));
    }

    /**
     * 안읽은 알림 개수 조회
     * GET /api/v1/notifications/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiSuccess<UnreadCountResponse>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        UnreadCountResponse response = notificationService.getUnreadCount(userId);
        
        return ResponseEntity.ok(ApiSuccess.of(response, "안읽은 알림 개수를 조회했습니다."));
    }

    /**
     * 특정 알림 읽음 처리
     * PUT /api/v1/notifications/{notificationId}/read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiSuccess<Void>> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long notificationId) {
        Long userId = userDetails.getUser().getId();
        notificationService.markAsRead(userId, notificationId);
        
        return ResponseEntity.ok(ApiSuccess.of(null, "알림을 읽음 처리했습니다."));
    }
}

