package likelion.harullala.dto;

import lombok.Builder;

@Builder
public record FeedbackDto(
        Long feedbackId,
        Long recordId,
        String aiReply,
        Attempts attempts,
        String createdAt, // ISO8601
        String updatedAt  // ISO8601
) {
    @Builder
    public record Attempts(int used, int remaining, int limit) {}
    
    public static FeedbackDtoBuilder builder() {
        return new FeedbackDtoBuilder();
    }
}