package likelion.harullala.dto;

import likelion.harullala.domain.EmojiEmotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 가장 많았던 감정 응답 DTO
 * 감정별 횟수와 대표 색상을 내림차순으로 반환
 */
@Getter
@AllArgsConstructor
@Builder
public class EmotionReportTopEmotionsResponse {
    
    private List<EmotionStat> emotions; // 감정 통계 목록 (내림차순)
    private Integer total_count;        // 전체 감정 기록 수
    
    /**
     * 개별 감정 통계
     */
    @Getter
    @AllArgsConstructor
    @Builder
    public static class EmotionStat {
        private String emotion_name;        // 감정명 (예: "만족스러움")
        private EmojiEmotion emoji_emotion; // 감정 카테고리
        private Integer count;              // 횟수
        private String color;               // 대표 색상 (가장 많이 사용된 Main Color)
        private Double percentage;          // 비율 (예: 35.5)
    }
}

