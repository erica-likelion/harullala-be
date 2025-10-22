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

    public static EmotionDeleteResponse of(Long recordId) {
        return EmotionDeleteResponse.builder()
                .record_id(recordId)
                .is_deleted(true) // 삭제 완료를 의미
                .build();
    }
}



