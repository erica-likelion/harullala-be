package likelion.harullala.dto;

import likelion.harullala.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MyInfoResponse {
    private Long userId;
    private String name;
    private CharacterInfo characterInfo;

    public static MyInfoResponse of(User user, CharacterInfo characterInfo) {
        return new MyInfoResponse(user.getId(), user.getUserName(), characterInfo);
    }
}
