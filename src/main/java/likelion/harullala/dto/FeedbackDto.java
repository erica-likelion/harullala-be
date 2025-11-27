package likelion.harullala.dto;

import lombok.Builder;

@Builder
public record FeedbackDto(
        Long feedbackId,
        Long recordId,
        String aiReply,
        Integer attemptsUsed, // 시도 횟수 (사용한 횟수, 숫자만 반환)
        Long characterId, // 피드백 생성 시 사용된 캐릭터 ID
        String characterName, // 피드백 생성 시 사용된 캐릭터 이름
        String characterImageUrl, // 피드백 생성 시 사용된 캐릭터 이미지 URL
        String createdAt, // ISO8601
        String updatedAt  // ISO8601
) {
    public static FeedbackDtoBuilder builder() {
        return new FeedbackDtoBuilder();
    }
}