package likelion.harullala.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ai_feedback", uniqueConstraints = @UniqueConstraint(columnNames = "record_id"))
@Getter
@Setter
public class AiFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    @Column(nullable = false, unique = true)
    private Long recordId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "text")
    private String aiReply;

    @Column(nullable = false)
    private Integer attemptsUsed = 1;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();
}