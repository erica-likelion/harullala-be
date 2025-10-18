package likelion.harullala.service;

import likelion.harullala.domain.EmojiEmotion;
import likelion.harullala.dto.EmotionRecommendRequest;
import likelion.harullala.dto.EmotionRecommendResponse;
import likelion.harullala.dto.EmotionRecommendResponse.EmotionSuggestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 감정 추천 서비스
 * 색상/좌표 기반으로 적절한 감정들을 추천
 */
@Service
@RequiredArgsConstructor
public class EmotionRecommendService {

    /**
     * 색상/좌표 기반으로 감정 추천
     * @param request 색상 및 좌표 정보
     * @return 추천된 감정 목록 (4-5개)
     */
    public EmotionRecommendResponse recommendEmotions(EmotionRecommendRequest request) {
        Double x = request.getPosition_x(); // 0.0 ~ 1.0
        Double y = request.getPosition_y(); // 0.0 ~ 1.0

        // 좌표를 그리드 영역으로 변환 (10x10 그리드 예시)
        int gridX = (int) (x * 10);
        int gridY = (int) (y * 10);

        // 그리드 좌표에 따라 감정 추천
        List<EmotionSuggestion> suggestions = getEmotionsByGrid(gridX, gridY);

        return EmotionRecommendResponse.builder()
                .suggestions(suggestions)
                .build();
    }

    /**
     * 그리드 좌표에 따른 감정 매핑
     * 표 이미지를 참고하여 각 영역별로 감정을 매핑
     */
    private List<EmotionSuggestion> getEmotionsByGrid(int x, int y) {
        List<EmotionSuggestion> suggestions = new ArrayList<>();

        // ===== Y축: 위쪽(행복함) ~ 아래쪽(우울함) =====
        // ===== X축: 왼쪽(불안함) ~ 오른쪽(편안함) =====

        // 우측 상단 (행복 + 편안) - 행복한 영역
        if (x >= 7 && y <= 3) {
            suggestions.add(createSuggestion("행복함", EmojiEmotion.HAPPY));
            suggestions.add(createSuggestion("만족스러움", EmojiEmotion.HAPPY));
            suggestions.add(createSuggestion("여유로움", EmojiEmotion.CALM));
            suggestions.add(createSuggestion("포근함", EmojiEmotion.CALM));
        }
        // 좌측 상단 (행복 + 불안) - 흥분한 영역
        else if (x <= 3 && y <= 3) {
            suggestions.add(createSuggestion("흥분됨", EmojiEmotion.EXCITED));
            suggestions.add(createSuggestion("기대됨", EmojiEmotion.EXCITED));
            suggestions.add(createSuggestion("긴장됨", EmojiEmotion.ANXIOUS));
            suggestions.add(createSuggestion("두근거림", EmojiEmotion.EXCITED));
        }
        // 우측 하단 (우울 + 편안) - 차분한 영역
        else if (x >= 7 && y >= 7) {
            suggestions.add(createSuggestion("우울함", EmojiEmotion.SAD));
            suggestions.add(createSuggestion("차분함", EmojiEmotion.CALM));
            suggestions.add(createSuggestion("평온함", EmojiEmotion.CALM));
            suggestions.add(createSuggestion("고요함", EmojiEmotion.CALM));
        }
        // 좌측 하단 (우울 + 불안) - 불안한 영역
        else if (x <= 3 && y >= 7) {
            suggestions.add(createSuggestion("불안함", EmojiEmotion.ANXIOUS));
            suggestions.add(createSuggestion("초조함", EmojiEmotion.ANXIOUS));
            suggestions.add(createSuggestion("우울함", EmojiEmotion.SAD));
            suggestions.add(createSuggestion("절망적임", EmojiEmotion.SAD));
        }
        // 중앙 상단 (행복)
        else if (y <= 3) {
            suggestions.add(createSuggestion("행복함", EmojiEmotion.HAPPY));
            suggestions.add(createSuggestion("즐거움", EmojiEmotion.HAPPY));
            suggestions.add(createSuggestion("기쁨", EmojiEmotion.HAPPY));
            suggestions.add(createSuggestion("뿌듯함", EmojiEmotion.HAPPY));
        }
        // 중앙 하단 (우울)
        else if (y >= 7) {
            suggestions.add(createSuggestion("우울함", EmojiEmotion.SAD));
            suggestions.add(createSuggestion("슬픔", EmojiEmotion.SAD));
            suggestions.add(createSuggestion("외로움", EmojiEmotion.LONELY));
            suggestions.add(createSuggestion("허전함", EmojiEmotion.SAD));
        }
        // 좌측 중앙 (불안)
        else if (x <= 3) {
            suggestions.add(createSuggestion("불안함", EmojiEmotion.ANXIOUS));
            suggestions.add(createSuggestion("초조함", EmojiEmotion.ANXIOUS));
            suggestions.add(createSuggestion("걱정됨", EmojiEmotion.ANXIOUS));
            suggestions.add(createSuggestion("긴장됨", EmojiEmotion.ANXIOUS));
        }
        // 우측 중앙 (편안)
        else if (x >= 7) {
            suggestions.add(createSuggestion("편안함", EmojiEmotion.CALM));
            suggestions.add(createSuggestion("평온함", EmojiEmotion.CALM));
            suggestions.add(createSuggestion("안정적임", EmojiEmotion.CALM));
            suggestions.add(createSuggestion("차분함", EmojiEmotion.CALM));
        }
        // 중앙 (중립)
        else {
            suggestions.add(createSuggestion("평범함", EmojiEmotion.CALM));
            suggestions.add(createSuggestion("그저 그럼", EmojiEmotion.CALM));
            suggestions.add(createSuggestion("무덤덤함", EmojiEmotion.CALM));
            suggestions.add(createSuggestion("담담함", EmojiEmotion.CALM));
        }

        // 4-5개만 반환
        return suggestions.subList(0, Math.min(5, suggestions.size()));
    }

    /**
     * EmotionSuggestion 생성 헬퍼 메서드
     */
    private EmotionSuggestion createSuggestion(String emotionName, EmojiEmotion category) {
        return EmotionSuggestion.builder()
                .emotion_name(emotionName)
                .emoji_emotion(category)
                .description(null) // 필요시 설명 추가
                .build();
    }
}

