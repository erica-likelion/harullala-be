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
    private EmojiEmotion emoji_emotion;
    private LocalDateTime created_at;

    public static EmotionListResponse from(EmotionRecord emotionRecord) {
        return EmotionListResponse.builder()
                .record_id(emotionRecord.getRecordId())
                .record(emotionRecord.getRecord())
                .emoji_emotion(emotionRecord.getEmojiEmotion())
                .created_at(emotionRecord.getCreatedAt())
                .build();
    }
}

