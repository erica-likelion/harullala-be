package likelion.harullala.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmotionShareRequest {

    @NotNull(message = "공유 상태를 입력해주세요.")
    private Boolean is_shared;
}

