package likelion.harullala.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 안읽은 알림 개수 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountResponse {
    private long unreadCount;
}

