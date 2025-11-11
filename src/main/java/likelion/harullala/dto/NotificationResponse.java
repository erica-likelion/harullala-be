package likelion.harullala.dto;

import java.time.LocalDateTime;

import likelion.harullala.domain.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long notificationId;
    private NotificationType type;
    private String title;
    private String message;
    private Long relatedId;
    private Boolean isRead;
    private LocalDateTime readAt;  // 읽은 시간
    private LocalDateTime createdAt;
}

