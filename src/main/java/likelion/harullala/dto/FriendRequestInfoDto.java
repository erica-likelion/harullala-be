package likelion.harullala.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestInfoDto {
    private Long requestId;
    private Long requesterId;
    private String requesterNickname;
    private String requesterConnectCode;
    private Long receiverId;
    private String receiverNickname;
    private String receiverConnectCode;
    private String status;
    private LocalDateTime createdAt;
}
