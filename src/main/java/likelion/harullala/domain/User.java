package likelion.harullala.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name="uq_provider_identity", columnNames = {"provider", "provider_user_id"}),
                @UniqueConstraint(name="uq_email_notnull", columnNames = {"email"}) // NULL은 중복 허용
        },
        indexes = {
                @Index(name="idx_users_provider", columnList = "provider"),
                @Index(name="idx_users_created_at", columnList = "created_at")
        }
)
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_name", length = 100, nullable = false)
    private String userName;

    @Column(name = "email", length = 255) // NULL 허용
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private Provider provider;

    @Column(name = "provider_user_id", nullable = false, length = 191)
    private String providerUserId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCharacter> userCharacters = new ArrayList<>();
}
