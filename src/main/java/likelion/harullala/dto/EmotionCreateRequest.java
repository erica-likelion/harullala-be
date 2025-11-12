package likelion.harullala.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmotionCreateRequest {

    @NotBlank(message = "감정 문장을 입력해주세요.")
    private String record; // 감정 기록 텍스트

    @NotBlank(message = "감정명을 선택해주세요.")
    private String emotion_name; // 구체적인 감정명 (예: "만족스러움")

    // 색상 정보 (Main, Sub, Text)
    private String main_color; // Main 색상 HEX 코드 (예: "#FF5733")
    private String sub_color;  // Sub 색상 HEX 코드 (예: "#3357FF")
    private String text_color; // Text 색상 HEX 코드 (예: "#FFFFFF")

    @NotNull(message = "공유 여부를 선택해주세요.")
    private Boolean is_shared; // 친구 공개 여부

    // AI 피드백 생성 횟수 (기본값 0)
    private Integer ai_feedback_count; // AI 피드백 생성 횟수
}


