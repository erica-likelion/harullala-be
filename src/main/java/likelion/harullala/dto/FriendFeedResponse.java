package likelion.harullala.dto;

import java.time.LocalDateTime;

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
    private String emotionName; // 감정명
    private String mainColor; // Main 색상
    private String subColor; // Sub 색상
    private String textColor; // Text 색상
    private LocalDateTime createdAt;
    private String authorNickname; // 작성자 닉네임
    private String authorProfileImageUrl; // 작성자 프로필 이미지 URL
    private boolean isRead; // 현재 사용자가 읽었는지 여부
    private long readCount; // 총 읽은 사람 수

    public static FriendFeedResponse from(EmotionRecord emotionRecord, String authorNickname, String authorProfileImageUrl, boolean isRead, long readCount) {
        return FriendFeedResponse.builder()
                .recordId(emotionRecord.getRecordId())
                .record(emotionRecord.getRecord())
                .emotionName(emotionRecord.getEmotionName())
                .mainColor(emotionRecord.getMainColor())
                .subColor(emotionRecord.getSubColor())
                .textColor(emotionRecord.getTextColor())
                .createdAt(emotionRecord.getCreatedAt())
                .authorNickname(authorNickname)
                .authorProfileImageUrl(authorProfileImageUrl)
                .isRead(isRead)
                .readCount(readCount)
                .build();
    }
}
