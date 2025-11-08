package likelion.harullala.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 감정명 → 좌표 매핑 유틸리티
 * 7x7 감정 표 기반으로 각 감정마다 미리 정의된 좌표값 제공
 * 
 * X축(가로): 매우 우울한(0.0) → 극도로 행복한(1.0)
 * Y축(세로): 완전 편안한(0.0) → 매우 불안한(1.0)
 */
public class EmotionCoordinateMapper {

    private static final Map<String, Coordinate> EMOTION_COORDINATES = new HashMap<>();

    static {
        // 좌표 계산: 7x7 그리드
        // X: 0.0, 0.17, 0.33, 0.5, 0.67, 0.83, 1.0
        // Y: 0.0, 0.17, 0.33, 0.5, 0.67, 0.83, 1.0
        
        // ========== 1행: 매우 불안한 (Y=1.0) ==========
        EMOTION_COORDINATES.put("초조한", new Coordinate(0.0, 1.0));
        EMOTION_COORDINATES.put("답답한", new Coordinate(0.17, 1.0));
        EMOTION_COORDINATES.put("근심있는", new Coordinate(0.33, 1.0));
        EMOTION_COORDINATES.put("불안한", new Coordinate(0.5, 1.0));
        EMOTION_COORDINATES.put("설레는", new Coordinate(0.67, 1.0));
        EMOTION_COORDINATES.put("두근거리는", new Coordinate(0.83, 1.0));
        EMOTION_COORDINATES.put("벅차는", new Coordinate(1.0, 1.0));
        
        // ========== 2행: 불안한 (Y=0.83) ==========
        EMOTION_COORDINATES.put("걱정되는", new Coordinate(0.0, 0.83));
        EMOTION_COORDINATES.put("불편한", new Coordinate(0.17, 0.83));
        EMOTION_COORDINATES.put("조바심나는", new Coordinate(0.33, 0.83));
        EMOTION_COORDINATES.put("안도하는", new Coordinate(0.5, 0.83));
        EMOTION_COORDINATES.put("후련한", new Coordinate(0.67, 0.83));
        EMOTION_COORDINATES.put("뿌듯한", new Coordinate(0.83, 0.83));
        EMOTION_COORDINATES.put("온화한", new Coordinate(1.0, 0.83));
        
        // ========== 3행: 조금 불안한 (Y=0.67) ==========
        EMOTION_COORDINATES.put("서운한", new Coordinate(0.0, 0.67));
        EMOTION_COORDINATES.put("무덤담한", new Coordinate(0.17, 0.67));
        EMOTION_COORDINATES.put("담담한", new Coordinate(0.33, 0.67));
        EMOTION_COORDINATES.put("괜찮은", new Coordinate(0.5, 0.67));
        EMOTION_COORDINATES.put("안심되는", new Coordinate(0.67, 0.67));
        EMOTION_COORDINATES.put("보람찬", new Coordinate(0.83, 0.67));
        EMOTION_COORDINATES.put("포근한", new Coordinate(1.0, 0.67));
        
        // ========== 4행: 조금 편안한 (Y=0.5) ==========
        EMOTION_COORDINATES.put("우울한", new Coordinate(0.0, 0.5));
        EMOTION_COORDINATES.put("평온한", new Coordinate(0.17, 0.5));
        EMOTION_COORDINATES.put("잔잔한", new Coordinate(0.33, 0.5));
        EMOTION_COORDINATES.put("어유로운", new Coordinate(0.5, 0.5));
        EMOTION_COORDINATES.put("좋은", new Coordinate(0.67, 0.5));
        EMOTION_COORDINATES.put("흐뭇한", new Coordinate(0.83, 0.5));
        EMOTION_COORDINATES.put("행복한", new Coordinate(1.0, 0.5));
        
        // ========== 5행: 편안한 (Y=0.33) ==========
        EMOTION_COORDINATES.put("체념한", new Coordinate(0.0, 0.33));
        EMOTION_COORDINATES.put("차분한", new Coordinate(0.17, 0.33));
        EMOTION_COORDINATES.put("안정적인", new Coordinate(0.33, 0.33));
        EMOTION_COORDINATES.put("따뜻한", new Coordinate(0.5, 0.33));
        EMOTION_COORDINATES.put("유쾌한", new Coordinate(0.67, 0.33));
        EMOTION_COORDINATES.put("고마운", new Coordinate(0.83, 0.33));
        EMOTION_COORDINATES.put("사랑스러운", new Coordinate(1.0, 0.33));
        
        // ========== 6행: 매우 편안한 (Y=0.17) ==========
        EMOTION_COORDINATES.put("평화로운", new Coordinate(0.0, 0.17));
        EMOTION_COORDINATES.put("너그러운", new Coordinate(0.17, 0.17));
        EMOTION_COORDINATES.put("고요한", new Coordinate(0.33, 0.17));
        EMOTION_COORDINATES.put("느긋한", new Coordinate(0.5, 0.17));
        EMOTION_COORDINATES.put("만족한", new Coordinate(0.67, 0.17));
        EMOTION_COORDINATES.put("기쁨", new Coordinate(0.83, 0.17));
        EMOTION_COORDINATES.put("황홀한", new Coordinate(1.0, 0.17));
        
        // ========== 7행: 완전 편안한 (Y=0.0) ==========
        EMOTION_COORDINATES.put("차로운", new Coordinate(0.0, 0.0));
        EMOTION_COORDINATES.put("온전한", new Coordinate(0.17, 0.0));
        EMOTION_COORDINATES.put("가벼운", new Coordinate(0.33, 0.0));
        EMOTION_COORDINATES.put("편안한", new Coordinate(0.5, 0.0));
        EMOTION_COORDINATES.put("충족한", new Coordinate(0.67, 0.0));
        EMOTION_COORDINATES.put("감사한", new Coordinate(0.83, 0.0));
        EMOTION_COORDINATES.put("환상적인", new Coordinate(1.0, 0.0));
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

