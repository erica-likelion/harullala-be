package likelion.harullala.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import likelion.harullala.domain.EmotionRecord;

@Repository
public interface EmotionRecordRepository extends JpaRepository<EmotionRecord, Long> {
    
    Page<EmotionRecord> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    Page<EmotionRecord> findByIsSharedTrueOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT e FROM EmotionRecord e WHERE e.userId = :userId " +
           "AND e.createdAt >= :startDate AND e.createdAt < :endDate " +
           "ORDER BY e.createdAt DESC")
    List<EmotionRecord> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(e) FROM EmotionRecord e WHERE e.userId = :userId " +
           "AND e.createdAt >= :startDate AND e.createdAt < :endDate")
    Long countByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT e FROM EmotionRecord e WHERE e.isShared = true " +
           "AND e.userId IN :userIds " +
           "AND e.createdAt >= :startDate " +
           "ORDER BY e.createdAt DESC")
    Page<EmotionRecord> findByIsSharedTrueAndUserIdInAndCreatedAtAfter(
            @Param("userIds") List<Long> userIds,
            @Param("startDate") LocalDateTime startDate,
            Pageable pageable
    );

    @Modifying
    @Query("DELETE FROM EmotionRecord e WHERE e.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
