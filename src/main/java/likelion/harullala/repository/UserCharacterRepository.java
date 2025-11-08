package likelion.harullala.repository;

import likelion.harullala.domain.UserCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserCharacterRepository extends JpaRepository<UserCharacter, Long> {
    boolean existsByUserId(Long userId);
    Optional<UserCharacter> findByUserId(Long userId);
    
    /**
     * 사용자의 활성 캐릭터 조회
     */
    @Query("SELECT uc FROM UserCharacter uc " +
           "JOIN FETCH uc.selectedCharacter " +
           "WHERE uc.user.userId = :userId AND uc.active = true")
    Optional<UserCharacter> findActiveByUserId(@Param("userId") Long userId);
}
