package likelion.harullala.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import likelion.harullala.domain.EmojiEmotion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmotionCreateRequest {

    @NotBlank(message = "감정 문장을 입력해주세요.")
    private String record;

    @NotNull(message = "감정 이모지를 선택해주세요.")
    private EmojiEmotion emoji_emotion;
}


