package likelion.harullala.dto;

import likelion.harullala.domain.EmotionRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class EmotionListResponse {
    private Long record_id;
    private String record;
    private String emotion_name; // 구체적인 감정명
    private String main_color; // Main 색상
    private String sub_color; // Sub 색상
    private String text_color; // Text 색상
    private Boolean is_shared;
    private Integer ai_feedback_count; // AI 피드백 생성 횟수
    private LocalDateTime created_at;

    public static EmotionListResponse from(EmotionRecord emotionRecord) {
        return EmotionListResponse.builder()
                .record_id(emotionRecord.getRecordId())
                .record(emotionRecord.getRecord())
                .emotion_name(emotionRecord.getEmotionName())
                .main_color(emotionRecord.getMainColor())
                .sub_color(emotionRecord.getSubColor())
                .text_color(emotionRecord.getTextColor())
                .is_shared(emotionRecord.getIsShared())
                .ai_feedback_count(emotionRecord.getAiFeedbackCount())
                .created_at(emotionRecord.getCreatedAt())
                .build();
    }

    public static EmotionListResponse from(EmotionRecord emotionRecord, String decryptedRecord) {
        return EmotionListResponse.builder()
                .record_id(emotionRecord.getRecordId())
                .record(decryptedRecord)
                .emotion_name(emotionRecord.getEmotionName())
                .main_color(emotionRecord.getMainColor())
                .sub_color(emotionRecord.getSubColor())
                .text_color(emotionRecord.getTextColor())
                .is_shared(emotionRecord.getIsShared())
                .ai_feedback_count(emotionRecord.getAiFeedbackCount())
                .created_at(emotionRecord.getCreatedAt())
                .build();
    }
}

