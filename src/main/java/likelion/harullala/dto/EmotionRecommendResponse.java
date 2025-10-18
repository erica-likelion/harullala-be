package likelion.harullala.dto;

import likelion.harullala.domain.EmojiEmotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 감정 추천 응답 DTO
 * 선택한 색상/좌표에 해당하는 감정들을 추천
 */
@Getter
@AllArgsConstructor
@Builder
public class EmotionRecommendResponse {
    
    private List<EmotionSuggestion> suggestions; // 추천된 감정 목록 (4-5개)
    
    /**
     * 개별 감정 추천 항목
     */
    @Getter
    @AllArgsConstructor
    @Builder
    public static class EmotionSuggestion {
        private String emotion_name; // 감정명 (예: "만족스러움", "행복함")
        private EmojiEmotion emoji_emotion; // 감정 카테고리 (HAPPY, SAD 등)
        private String description; // 감정 설명 (선택적)
    }
}

