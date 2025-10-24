package likelion.harullala.dto;

import java.time.LocalDateTime;

import likelion.harullala.domain.EmojiEmotion;
import likelion.harullala.domain.EmotionRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendFeedResponse {

    private Long recordId;
    private String record;
    private EmojiEmotion emojiEmotion;
    private LocalDateTime createdAt;
    private String authorNickname; // 작성자 닉네임
    private boolean isRead; // 현재 사용자가 읽었는지 여부
    private long readCount; // 총 읽은 사람 수

    public static FriendFeedResponse from(EmotionRecord emotionRecord, String authorNickname, boolean isRead, long readCount) {
        return FriendFeedResponse.builder()
                .recordId(emotionRecord.getRecordId())
                .record(emotionRecord.getRecord())
                .emojiEmotion(emotionRecord.getEmojiEmotion())
                .createdAt(emotionRecord.getCreatedAt())
                .authorNickname(authorNickname)
                .isRead(isRead)
                .readCount(readCount)
                .build();
    }
}
