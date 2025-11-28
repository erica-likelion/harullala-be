package likelion.harullala.dto;

import likelion.harullala.domain.EmotionRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class EmotionUpdateResponse {
    private Long record_id;
    private Long user_id;
    private String record;
    private String emotion_name; // 구체적인 감정명
    private String main_color; // Main 색상
    private String sub_color; // Sub 색상
    private String text_color; // Text 색상
    private Boolean is_shared; // 친구 공개 여부
    private Integer ai_feedback_count; // AI 피드백 생성 횟수
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public static EmotionUpdateResponse from(EmotionRecord emotionRecord) {
        return EmotionUpdateResponse.builder()
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

    public static EmotionUpdateResponse from(EmotionRecord emotionRecord, String decryptedRecord) {
        return EmotionUpdateResponse.builder()
                .record_id(emotionRecord.getRecordId())
                .user_id(emotionRecord.getUserId())
                .record(decryptedRecord)
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



