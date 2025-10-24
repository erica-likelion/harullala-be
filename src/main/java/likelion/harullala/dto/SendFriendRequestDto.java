package likelion.harullala.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SendFriendRequestDto {
    private String connectCode; // 친구의 초대 코드
}
