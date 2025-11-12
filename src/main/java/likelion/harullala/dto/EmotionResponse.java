package likelion.harullala.dto;

import likelion.harullala.domain.EmotionRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class EmotionResponse {
    private Long record_id;
    private Long user_id;
    private String record;
    private String emotion_name; // 구체적인 감정명
    private String main_color; // Main 색상
    private String sub_color; // Sub 색상
    private String text_color; // Text 색상
    private Boolean is_shared;
    private Integer ai_feedback_count; // AI 피드백 생성 횟수
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public static EmotionResponse from(EmotionRecord emotionRecord) {
        return EmotionResponse.builder()
                .record_id(emotionRecord.getRecordId())
                .user_id(emotionRecord.getUserId())
                .record(emotionRecord.getRecord())
                .emotion_name(emotionRecord.getEmotionName())
                .main_color(emotionRecord.getMainColor())
                .sub_color(emotionRecord.getSubColor())
                .text_color(emotionRecord.getTextColor())
                .is_shared(emotionRecord.getIsShared())
                .ai_feedback_count(emotionRecord.getAiFeedbackCount())
                .created_at(emotionRecord.getCreatedAt())
                .updated_at(emotionRecord.getUpdatedAt())
                .build();
    }
}


