package likelion.harullala.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 감정 추천 요청 DTO
 * 사용자가 그라디언트 맵에서 색상을 선택하면 해당 좌표/색상 정보를 보냄
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmotionRecommendRequest {
    
    @NotNull(message = "Main 색상을 선택해주세요.")
    private String main_color; // Main 색상 HEX 코드 (예: "#FF5733")
    
    @NotNull(message = "Sub 색상을 선택해주세요.")
    private String sub_color; // Sub 색상 HEX 코드 (예: "#3357FF")
    
    @NotNull(message = "X 좌표를 입력해주세요.")
    private Double position_x; // X축 좌표 (0.0 ~ 1.0 정규화 값)
    
    @NotNull(message = "Y 좌표를 입력해주세요.")
    private Double position_y; // Y축 좌표 (0.0 ~ 1.0 정규화 값)
}

