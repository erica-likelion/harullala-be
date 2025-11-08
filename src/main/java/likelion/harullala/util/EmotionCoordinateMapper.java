package likelion.harullala.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 감정명 → 좌표 매핑 유틸리티
 * 각 감정마다 미리 정의된 고정 좌표값 제공
 */
public class EmotionCoordinateMapper {

    private static final Map<String, Coordinate> EMOTION_COORDINATES = new HashMap<>();

    static {
        // 행복한 감정들 (우측 상단)
        EMOTION_COORDINATES.put("행복함", new Coordinate(0.85, 0.15));
        EMOTION_COORDINATES.put("만족스러움", new Coordinate(0.80, 0.20));
        EMOTION_COORDINATES.put("즐거움", new Coordinate(0.85, 0.25));
        EMOTION_COORDINATES.put("기쁨", new Coordinate(0.80, 0.15));
        EMOTION_COORDINATES.put("뿌듯함", new Coordinate(0.75, 0.20));
        
        // 편안한 감정들 (우측 중앙)
        EMOTION_COORDINATES.put("편안함", new Coordinate(0.85, 0.50));
        EMOTION_COORDINATES.put("평온함", new Coordinate(0.80, 0.45));
        EMOTION_COORDINATES.put("안정적임", new Coordinate(0.85, 0.55));
        EMOTION_COORDINATES.put("차분함", new Coordinate(0.80, 0.50));
        EMOTION_COORDINATES.put("여유로움", new Coordinate(0.75, 0.45));
        EMOTION_COORDINATES.put("포근함", new Coordinate(0.75, 0.40));
        
        // 흥분된 감정들 (좌측 상단)
        EMOTION_COORDINATES.put("흥분됨", new Coordinate(0.25, 0.20));
        EMOTION_COORDINATES.put("기대됨", new Coordinate(0.20, 0.25));
        EMOTION_COORDINATES.put("두근거림", new Coordinate(0.30, 0.20));
        
        // 불안한 감정들 (좌측 중앙~하단)
        EMOTION_COORDINATES.put("불안함", new Coordinate(0.20, 0.60));
        EMOTION_COORDINATES.put("초조함", new Coordinate(0.25, 0.65));
        EMOTION_COORDINATES.put("걱정됨", new Coordinate(0.20, 0.55));
        EMOTION_COORDINATES.put("긴장됨", new Coordinate(0.25, 0.50));
        
        // 우울한 감정들 (하단)
        EMOTION_COORDINATES.put("우울함", new Coordinate(0.50, 0.85));
        EMOTION_COORDINATES.put("슬픔", new Coordinate(0.45, 0.80));
        EMOTION_COORDINATES.put("외로움", new Coordinate(0.55, 0.85));
        EMOTION_COORDINATES.put("허전함", new Coordinate(0.50, 0.80));
        EMOTION_COORDINATES.put("절망적임", new Coordinate(0.30, 0.85));
        
        // 중립 감정들 (중앙)
        EMOTION_COORDINATES.put("평범함", new Coordinate(0.50, 0.50));
        EMOTION_COORDINATES.put("그저 그럼", new Coordinate(0.55, 0.50));
        EMOTION_COORDINATES.put("무덤덤함", new Coordinate(0.50, 0.55));
        EMOTION_COORDINATES.put("담담함", new Coordinate(0.45, 0.50));
        
        // 화난 감정들 (좌측 상단)
        EMOTION_COORDINATES.put("화남", new Coordinate(0.15, 0.30));
        EMOTION_COORDINATES.put("짜증남", new Coordinate(0.20, 0.35));
        
        // 지친 감정들 (우측 하단)
        EMOTION_COORDINATES.put("지침", new Coordinate(0.70, 0.75));
        EMOTION_COORDINATES.put("피곤함", new Coordinate(0.65, 0.70));
    }

    /**
     * 감정명으로 좌표 가져오기
     * @param emotionName 감정명
     * @return 좌표 (없으면 중앙값 0.5, 0.5)
     */
    public static Coordinate getCoordinate(String emotionName) {
        return EMOTION_COORDINATES.getOrDefault(emotionName, new Coordinate(0.5, 0.5));
    }

    /**
     * 감정명에 매핑된 좌표가 있는지 확인
     */
    public static boolean hasMapping(String emotionName) {
        return EMOTION_COORDINATES.containsKey(emotionName);
    }

    /**
     * 좌표 클래스
     */
    public static class Coordinate {
        private final double x;
        private final double y;

        public Coordinate(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }
}

