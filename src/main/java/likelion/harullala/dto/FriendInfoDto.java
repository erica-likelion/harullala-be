package likelion.harullala.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FriendInfoDto {
    private Long userId;
    private String nickname;
    private String connectCode;
    private String name;
}
