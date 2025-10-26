package likelion.harullala.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RespondToFriendRequestDto {
    private Long requestId;
    private boolean accept; // true: 수락, false: 거절
}
