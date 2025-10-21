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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "friend_requests",
        indexes = {
                @Index(name = "idx_friend_request_requester", columnList = "requester_id"),
                @Index(name = "idx_friend_request_receiver", columnList = "receiver_id"),
                @Index(name = "idx_friend_request_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private FriendRequestStatus status = FriendRequestStatus.PENDING;

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
        this.status = FriendRequestStatus.ACCEPTED;
    }

    /**
     * 친구 요청 거절
     */
    public void reject() {
        this.status = FriendRequestStatus.REJECTED;
    }

    /**
     * 친구 요청 취소
     */
    public void cancel() {
        this.status = FriendRequestStatus.CANCELLED;
    }

    /**
     * 대기 중인 요청인지 확인
     */
    public boolean isPending() {
        return this.status == FriendRequestStatus.PENDING;
    }

    /**
     * 수락된 요청인지 확인
     */
    public boolean isAccepted() {
        return this.status == FriendRequestStatus.ACCEPTED;
    }
}
