package likelion.harullala.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SentFriendRequestDto {
    private Long requestId;
    private String receiverNickname;
    private String receiverConnectCode;
    private String receiverProfileImageUrl;
    private LocalDateTime createdAt;
}

