package likelion.harullala.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SendFriendRequestDto {
    // private String connectCode; // 유저 팀원이 findByConnectCode 메서드 추가 후 활성화 예정
    private Long userId; // 현재는 userId만 사용
}
