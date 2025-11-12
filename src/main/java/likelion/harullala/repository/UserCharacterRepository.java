package likelion.harullala.repository;

import likelion.harullala.domain.UserCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserCharacterRepository extends JpaRepository<UserCharacter, Long> {
    
    @Query("SELECT CASE WHEN COUNT(uc) > 0 THEN true ELSE false END FROM UserCharacter uc WHERE uc.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT uc FROM UserCharacter uc WHERE uc.user.id = :userId")
    Optional<UserCharacter> findByUserId(@Param("userId") Long userId);

    /**
     */
    @Query("SELECT uc FROM UserCharacter uc " +
           "JOIN FETCH uc.selectedCharacter " +
           "WHERE uc.user.id = :userId")
    Optional<UserCharacter> findActiveByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserCharacter uc WHERE uc.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
