package likelion.harullala.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feed_read_status", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"reader_id", "record_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FeedReadStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "read_id")
    private Long readId;

    @Column(name = "reader_id", nullable = false)
    private Long readerId; // 피드를 읽은 사용자 ID

    @Column(name = "record_id", nullable = false)
    private Long recordId; // 읽은 감정 기록 ID

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt; // 읽은 시간

    @PrePersist
    protected void onCreate() {
        readAt = LocalDateTime.now();
    }
}
