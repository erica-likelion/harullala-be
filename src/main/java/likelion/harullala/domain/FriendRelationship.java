package likelion.harullala.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "friend_relationships",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_friend_relationship", columnNames = {"user1_id", "user2_id"})
        },
        indexes = {
                @Index(name = "idx_fr_user1", columnList = "user1_id"),
                @Index(name = "idx_fr_user2", columnList = "user2_id"),
                @Index(name = "idx_fr_status", columnList = "status"),
                @Index(name = "idx_fr_requester", columnList = "requester_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FriendRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "relationship_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1; // 항상 user1.id < user2.id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2; // 항상 user1.id > user2.id

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private FriendStatus status = FriendStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester; // 요청을 보낸 사람

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 친구 요청 수락
     */
    public void accept() {
        this.status = FriendStatus.ACCEPTED;
    }

    /**
     * 친구 요청 거절
     */
    public void reject() {
        this.status = FriendStatus.REJECTED;
    }

    /**
     * 친구 요청 취소
     */
    public void cancel() {
        this.status = FriendStatus.CANCELLED;
    }

    /**
     * 대기 중인 요청인지 확인
     */
    public boolean isPending() {
        return this.status == FriendStatus.PENDING;
    }

    /**
     * 수락된 요청인지 확인 (친구 관계)
     */
    public boolean isAccepted() {
        return this.status == FriendStatus.ACCEPTED;
    }

    /**
     * 특정 사용자가 이 관계에 포함되어 있는지 확인
     */
    public boolean containsUser(Long userId) {
        return user1.getId().equals(userId) || user2.getId().equals(userId);
    }

    /**
     * 특정 사용자의 상대방 ID 반환
     */
    public Long getOtherUserId(Long userId) {
        if (user1.getId().equals(userId)) {
            return user2.getId();
        } else if (user2.getId().equals(userId)) {
            return user1.getId();
        }
        throw new IllegalArgumentException("User is not part of this relationship");
    }

    /**
     * 특정 사용자의 상대방 User 객체 반환
     */
    public User getOtherUser(Long userId) {
        if (user1.getId().equals(userId)) {
            return user2;
        } else if (user2.getId().equals(userId)) {
            return user1;
        }
        throw new IllegalArgumentException("User is not part of this relationship");
    }

    /**
     * 이 관계가 누가 요청했는지 확인
     */
    public boolean isRequester(Long userId) {
        return requester.getId().equals(userId);
    }

    /**
     * 이 관계가 누가 받았는지 확인
     */
    public boolean isReceiver(Long userId) {
        return !isRequester(userId) && containsUser(userId);
    }
}

