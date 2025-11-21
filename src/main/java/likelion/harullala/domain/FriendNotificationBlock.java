package likelion.harullala.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 친구 푸시 알림 차단 엔티티
 * 사용자가 특정 친구의 푸시 알림을 차단할 수 있도록 관리
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "friend_notification_blocks",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_user_blocked_friend", columnNames = {"user_id", "blocked_friend_id"})
        },
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_blocked_friend_id", columnList = "blocked_friend_id")
        })
public class FriendNotificationBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "block_id")
    private Long blockId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 알림을 차단하는 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_friend_id", nullable = false)
    private User blockedFriend;  // 차단된 친구

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

