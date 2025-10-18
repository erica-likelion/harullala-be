package likelion.harullala.dto;

import likelion.harullala.domain.EmojiEmotion;
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
    private EmojiEmotion emoji_emotion; // 감정 카테고리
    private String emotion_name; // 구체적인 감정명
    private String main_color; // Main 색상
    private String sub_color; // Sub 색상
    private Boolean is_shared;
    private LocalDateTime created_at;

    public static EmotionListResponse from(EmotionRecord emotionRecord) {
        return EmotionListResponse.builder()
                .record_id(emotionRecord.getRecordId())
                .record(emotionRecord.getRecord())
                .emoji_emotion(emotionRecord.getEmojiEmotion())
                .emotion_name(emotionRecord.getEmotionName())
                .main_color(emotionRecord.getMainColor())
                .sub_color(emotionRecord.getSubColor())
                .is_shared(emotionRecord.getIsShared())
                .created_at(emotionRecord.getCreatedAt())
                .build();
    }
}

