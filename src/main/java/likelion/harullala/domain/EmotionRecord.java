package likelion.harullala.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity     // 감정기록 JAP 엔티티 - 데이터베이스 테이블과 매핑
@Table(name = "emotion_record") // 테이블 이름 지정
@Getter // 모든 필드에 대한 Getter 메서드 자동 생성 (getRecordId(), getUserId()등 ...)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 자동 생성 (AccessLevel.PROTECTED로 설정하여 외부에서 생성 불가)
@AllArgsConstructor // 모든 필드를 파라미터로 받는 생성자 자동 생성
@Builder // 빌더 패턴 구현 자동 생성
public class EmotionRecord {

    @Id // 기본 키 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성 전략 지정 (IDENTITY: 데이터베이스에서 자동 생성)
    @Column(name = "record_id")
    private Long recordId; // 감정기록 ID

    @Column(name = "user_id", nullable = false) // 사용자 ID 필드 (NOT NULL)
    private Long userId; // 사용자 ID

    @Column(nullable = false, columnDefinition = "TEXT") // 감정기록 필드 (NOT NULL, TEXT 타입)
    private String record; // 감정기록

    // ===== 색상 정보 (사용자가 선택한 Main, Sub, Text 색상) =====
    @Column(name = "main_color", length = 7) // Main 색상 (HEX 코드, 예: #FF5733)
    private String mainColor;

    @Column(name = "sub_color", length = 7) // Sub 색상 (HEX 코드, 예: #3357FF)
    private String subColor;

    @Column(name = "text_color", length = 7) // Text 색상 (HEX 코드, 예: #FFFFFF)
    private String textColor;

    // ===== 좌표 정보 (그라디언트 맵에서 선택한 위치) =====
    @Column(name = "position_x") // X축 좌표 (0.0 ~ 1.0 정규화 값 또는 실제 픽셀 값)
    private Double positionX;

    @Column(name = "position_y") // Y축 좌표 (0.0 ~ 1.0 정규화 값 또는 실제 픽셀 값)
    private Double positionY;

    // ===== 감정 정보 =====
    @Enumerated(EnumType.STRING) // 감정 카테고리 (ENUM 타입)
    @Column(name = "emoji_emotion", nullable = false) // 감정 카테고리 필드 (NOT NULL)
    private EmojiEmotion emojiEmotion; // 감정 카테고리 (HAPPY, SAD 등)

    @Column(name = "emotion_name", nullable = false, length = 50) // 사용자가 선택한 구체적인 감정명
    private String emotionName; // 구체적인 감정명 (예: "만족스러움", "행복함", "평온")

    @Column(name = "is_shared", nullable = false) // 공유 상태 필드 (NOT NULL)
    @Builder.Default // 빌더 패턴 기본값 설정
    private Boolean isShared = false; // 공유 상태

    @Column(name = "ai_feedback_count", nullable = false) // AI 피드백 생성 횟수 (NOT NULL)
    @Builder.Default // 빌더 패턴 기본값 설정
    private Integer aiFeedbackCount = 0; // AI 피드백 생성 횟수

    @Column(name = "created_at", nullable = false, updatable = false) // 생성 시간 필드 (NOT NULL, 수정 불가)
    private LocalDateTime createdAt; // 생성 시간

    @Column(name = "updated_at", nullable = false) // 수정 시간 필드 (NOT NULL)
    private LocalDateTime updatedAt; // 수정 시간

    @PrePersist // 저장 전 실행
    protected void onCreate() {
        createdAt = LocalDateTime.now(); // 생성 시간 설정
        updatedAt = LocalDateTime.now(); // 수정 시간 설정
    }

    @PreUpdate // 수정 전 실행
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(); // 수정 시간 설정
    }

    /**
     * 감정기록 업데이트 (전체)
     */
    public void update(String record, String emotionName, EmojiEmotion emojiEmotion, 
                      String mainColor, String subColor, String textColor, Double positionX, Double positionY,
                      Integer aiFeedbackCount) {
        this.record = record; // 감정기록 업데이트
        this.emotionName = emotionName; // 감정명 업데이트
        this.emojiEmotion = emojiEmotion; // 감정 카테고리 업데이트
        this.mainColor = mainColor; // Main 색상 업데이트
        this.subColor = subColor; // Sub 색상 업데이트
        this.textColor = textColor; // Text 색상 업데이트
        this.positionX = positionX; // X 좌표 업데이트
        this.positionY = positionY; // Y 좌표 업데이트
        if (aiFeedbackCount != null) {
            this.aiFeedbackCount = aiFeedbackCount; // AI 피드백 횟수 업데이트
        }
    }

    /**
     * 감정기록 텍스트만 업데이트 (간단 버전)
     */
    public void updateRecord(String record) {
        this.record = record; // 감정기록 텍스트만 업데이트
    }

    /**
     * 감정기록 공유 상태 변경
     */
    public void updateSharedStatus(Boolean isShared) {
        this.isShared = isShared; // 공유 상태 업데이트
    }

    /**
     * AI 피드백 생성 횟수 증가
     */
    public void incrementAiFeedbackCount() {
        this.aiFeedbackCount++; // AI 피드백 횟수 1 증가
    }
}


