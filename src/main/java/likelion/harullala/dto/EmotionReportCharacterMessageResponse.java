package likelion.harullala.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 감정 리포트 캐릭터 멘트 응답 DTO
 */
@Getter
@AllArgsConstructor
@Builder
public class EmotionReportCharacterMessageResponse {
    private String character_name;      // 캐릭터 이름
    private String character_image_url; // 캐릭터 이미지 URL
    private String message;             // AI가 생성한 캐릭터 멘트
    private Integer attempts_used;      // 사용한 시도 횟수
    private Integer attempts_remaining; // 남은 시도 횟수
    private Integer attempts_total;     // 전체 시도 가능 횟수
}

