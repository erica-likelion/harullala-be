package likelion.harullala.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 감정 상태 비교 응답 DTO
 * 저번 달과 이번 달의 평균 감정 상태 비교
 */
@Getter
@AllArgsConstructor
@Builder
public class EmotionReportComparisonResponse {
    
    private MonthlyAverage lastMonth;  // 저번 달 평균
    private MonthlyAverage thisMonth;  // 이번 달 평균
    
    /**
     * 월별 평균 감정 상태
     */
    @Getter
    @AllArgsConstructor
    @Builder
    public static class MonthlyAverage {
        private String month;           // 월 (예: "2024-01")
        private Double avg_position_x;  // X축 평균 (0.0 ~ 1.0)
        private Double avg_position_y;  // Y축 평균 (0.0 ~ 1.0)
        private String avg_color;       // 평균 위치의 대표 색상
        private String label;           // 라벨 (예: "저번 달 평균 상태", "이번 달 평균 상태")
        private Integer record_count;   // 해당 월의 총 기록 수
    }
}

