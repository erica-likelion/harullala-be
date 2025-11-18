package likelion.harullala.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReceivedFriendRequestDto {
    private Long requestId;
    private String requesterNickname;
    private String requesterConnectCode;
    private String requesterProfileImageUrl;
    private LocalDateTime createdAt;
}

