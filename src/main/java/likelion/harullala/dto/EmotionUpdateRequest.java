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
public class EmotionUpdateRequest {

    @NotBlank(message = "감정 문장을 입력해주세요.")
    private String record; // 감정 기록 텍스트

    @NotNull(message = "감정 카테고리를 선택해주세요.")
    private EmojiEmotion emoji_emotion; // 감정 카테고리 (HAPPY, SAD 등)

    @NotBlank(message = "감정명을 선택해주세요.")
    private String emotion_name; // 구체적인 감정명 (예: "만족스러움")

    // 색상 정보 (Main, Sub, Text)
    private String main_color; // Main 색상 HEX 코드 (예: "#FF5733")
    private String sub_color;  // Sub 색상 HEX 코드 (예: "#3357FF")
    private String text_color; // Text 색상 HEX 코드 (예: "#FFFFFF")

    // 좌표 정보 (그라디언트 맵 위치)
    private Double position_x; // X축 좌표
    private Double position_y; // Y축 좌표

    // 공유 여부
    private Boolean is_shared; // 친구 공개 여부

    // AI 피드백 생성 횟수
    private Integer ai_feedback_count; // AI 피드백 생성 횟수
}

