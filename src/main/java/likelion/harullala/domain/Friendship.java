package likelion.harullala.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "friendships",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_friendship", columnNames = {"user1_id", "user2_id"})
        },
        indexes = {
                @Index(name = "idx_friendship_user1", columnList = "user1_id"),
                @Index(name = "idx_friendship_user2", columnList = "user2_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friendship_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 특정 사용자가 이 친구 관계에 포함되어 있는지 확인
     */
    public boolean containsUser(Long userId) {
        return user1.getId().equals(userId) || user2.getId().equals(userId);
    }

    /**
     * 특정 사용자의 친구 ID 반환
     */
    public Long getFriendId(Long userId) {
        if (user1.getId().equals(userId)) {
            return user2.getId();
        } else if (user2.getId().equals(userId)) {
            return user1.getId();
        }
        throw new IllegalArgumentException("User is not part of this friendship");
    }
}
