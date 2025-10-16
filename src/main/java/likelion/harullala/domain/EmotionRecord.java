package likelion.harullala.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "emotion_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EmotionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String record;

    @Enumerated(EnumType.STRING)
    @Column(name = "emoji_emotion", nullable = false)
    private EmojiEmotion emojiEmotion;

    @Column(name = "is_shared", nullable = false)
    @Builder.Default
    private Boolean isShared = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 감정기록 업데이트
     */
    public void update(String record, EmojiEmotion emojiEmotion) {
        this.record = record;
        this.emojiEmotion = emojiEmotion;
    }

    /**
     * 감정기록 공유 상태 변경
     */
    public void updateSharedStatus(Boolean isShared) {
        this.isShared = isShared;
    }
}


