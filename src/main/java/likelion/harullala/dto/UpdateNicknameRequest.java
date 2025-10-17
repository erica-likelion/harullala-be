package likelion.harullala.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateNicknameRequest {
    @JsonProperty("nickname")
    private String nickname;
}
