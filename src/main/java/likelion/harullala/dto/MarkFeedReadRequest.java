package likelion.harullala.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MarkFeedReadRequest {

    @NotNull(message = "감정 기록 ID를 입력해주세요.")
    private Long recordId;
}
