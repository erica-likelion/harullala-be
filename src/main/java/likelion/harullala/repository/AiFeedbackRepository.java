package likelion.harullala.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import likelion.harullala.domain.AiFeedback;

@Repository
public interface AiFeedbackRepository extends JpaRepository<AiFeedback, Long> {
    Optional<AiFeedback> findByRecordId(Long recordId);

    @Modifying
    @Query("DELETE FROM AiFeedback a WHERE a.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}