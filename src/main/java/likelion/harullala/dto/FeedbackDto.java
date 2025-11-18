package likelion.harullala.dto;

import lombok.Builder;

@Builder
public record FeedbackDto(
        Long feedbackId,
        Long recordId,
        String aiReply,
        Integer attemptsUsed, // 시도 횟수 (사용한 횟수, 숫자만 반환)
        String createdAt, // ISO8601
        String updatedAt  // ISO8601
) {
    public static FeedbackDtoBuilder builder() {
        return new FeedbackDtoBuilder();
    }
}