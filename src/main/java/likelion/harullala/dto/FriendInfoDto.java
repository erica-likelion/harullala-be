package likelion.harullala.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FriendInfoDto {
    private String nickname;
    private String connectCode;
    private String profileImageUrl;
    private Boolean hasRecordedToday;
}
