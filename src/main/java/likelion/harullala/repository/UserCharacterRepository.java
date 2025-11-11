package likelion.harullala.repository;

import likelion.harullala.domain.UserCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserCharacterRepository extends JpaRepository<UserCharacter, Long> {
    boolean existsByUserId(Long userId);
    Optional<UserCharacter> findByUserId(Long userId);
    
    /**
     */
    @Query("SELECT uc FROM UserCharacter uc " +
           "JOIN FETCH uc.selectedCharacter " +
           "WHERE uc.user.id = :userId AND uc.active = true")
    Optional<UserCharacter> findActiveByUserId(@Param("userId") Long userId);

    @Modifying
    void deleteAllByUserId(@Param("userId") Long userId);
}
