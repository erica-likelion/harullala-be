package likelion.harullala.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 친구 푸시 알림 차단 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BlockFriendNotificationDto {
    private String connectCode;  // 차단할 친구의 connectCode
}

