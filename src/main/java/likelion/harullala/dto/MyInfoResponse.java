package likelion.harullala.dto;

import likelion.harullala.domain.Provider;
import likelion.harullala.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MyInfoResponse {
    private String nickname;
    private String email;
    private Provider provider;
    private String connectCode;
    private String profileImageUrl;
    private CharacterInfo characterInfo;


    public static MyInfoResponse of(User user, CharacterInfo characterInfo) {
        return new MyInfoResponse(
                user.getNickname(),
                user.getEmail(),
                user.getProvider(),
                user.getConnectCode(),
                user.getProfileImageUrl(),
                characterInfo
        );
    }
}
