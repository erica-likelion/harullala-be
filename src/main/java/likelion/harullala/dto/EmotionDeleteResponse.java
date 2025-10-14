package likelion.harullala.dto;

import likelion.harullala.domain.EmotionRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class EmotionDeleteResponse {
    private Long record_id;
    private Boolean is_deleted;

    public static EmotionDeleteResponse from(EmotionRecord emotionRecord) {
        return EmotionDeleteResponse.builder()
                .record_id(emotionRecord.getRecordId())
                .is_deleted(emotionRecord.getIsDeleted())
                .build();
    }
}


