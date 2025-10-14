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
public class EmotionResponse {
    private Long record_id;
    private Long user_id;
    private String record;
    private EmojiEmotion emoji_emotion;
    private Boolean is_deleted;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public static EmotionResponse from(EmotionRecord emotionRecord) {
        return EmotionResponse.builder()
                .record_id(emotionRecord.getRecordId())
                .user_id(emotionRecord.getUserId())
                .record(emotionRecord.getRecord())
                .emoji_emotion(emotionRecord.getEmojiEmotion())
                .is_deleted(emotionRecord.getIsDeleted())
                .created_at(emotionRecord.getCreatedAt())
                .updated_at(emotionRecord.getUpdatedAt())
                .build();
    }
}


