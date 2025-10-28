package likelion.harullala.domain;

import likelion.harullala.domain.Character;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "user_characters",
        indexes = {
                @Index(name="idx_uc_user", columnList = "user_id"),
                @Index(name="idx_uc_character", columnList = "character_id")
        }
)
public class UserCharacter {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name="fk_uc_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_id", nullable = false,
            foreignKey = @ForeignKey(name="fk_uc_character"))
    private Character selectedCharacter;

    @Column(name = "is_active", nullable = false)
    private boolean active;              // 현재 사용중

    @CreationTimestamp
    @Column(name = "selected_at", nullable = false, updatable = false)
    private LocalDateTime selectedAt;

    public void updateCharacter(Character newCharacter) {
        this.selectedCharacter = newCharacter;
    }
}
