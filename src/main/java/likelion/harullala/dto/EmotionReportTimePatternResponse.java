package likelion.harullala.dto;

import likelion.harullala.domain.EmojiEmotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 시간대별 감정 패턴 응답 DTO
 */
@Getter
@AllArgsConstructor
@Builder
public class EmotionReportTimePatternResponse {
    
    private String dominant_time;      // 가장 많이 기록한 시간대 (예: "낮")
    private String dominant_emotion;   // 그 시간대의 주요 감정 (예: "만족스러움")
    private Integer total_count;       // 전체 기록 수
    private List<TimeSlot> time_slots; // 시간대별 상세 정보
    
    /**
     * 시간대별 정보
     */
    @Getter
    @AllArgsConstructor
    @Builder
    public static class TimeSlot {
        private String time_range;        // 시간대 (예: "낮 (12:00-18:00)")
        private String emotion_name;      // 가장 많았던 감정명
        private EmojiEmotion emoji_emotion; // 감정 카테고리
        private Integer count;            // 해당 시간대 기록 수
        private Double percentage;        // 비율 (예: 42.5)
        private String color;             // 대표 색상
        private String text_color;        // 대표 텍스트 색상
    }
}

