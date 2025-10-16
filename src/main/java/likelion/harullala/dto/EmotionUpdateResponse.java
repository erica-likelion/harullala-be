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
public class EmotionUpdateResponse {
    private Long record_id;
    private String record;
    private EmojiEmotion emoji_emotion;
    private LocalDateTime updated_at;

    public static EmotionUpdateResponse from(EmotionRecord emotionRecord) {
        return EmotionUpdateResponse.builder()
                .record_id(emotionRecord.getRecordId())
                .record(emotionRecord.getRecord())
                .emoji_emotion(emotionRecord.getEmojiEmotion())
                .updated_at(emotionRecord.getUpdatedAt())
                .build();
    }
}



