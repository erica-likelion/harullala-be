package likelion.harullala.repository;

import likelion.harullala.domain.EmotionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmotionRecordRepository extends JpaRepository<EmotionRecord, Long> {
    
    /**
     * 특정 사용자의 감정기록을 조회 (페이지네이션)
     * 최신순 정렬 (created_at DESC)
     */
    Page<EmotionRecord> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 공유된 감정기록을 조회 (페이지네이션)
     * 최신순 정렬 (created_at DESC)
     */
    Page<EmotionRecord> findByIsSharedTrueOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 특정 사용자의 특정 기간 동안의 감정 기록 조회
     * @param userId 사용자 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 감정 기록 목록
     */
    @Query("SELECT e FROM EmotionRecord e WHERE e.userId = :userId " +
           "AND e.createdAt >= :startDate AND e.createdAt < :endDate " +
           "ORDER BY e.createdAt DESC")
    List<EmotionRecord> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 특정 사용자의 특정 기간 동안의 감정 기록 개수 조회
     */
    @Query("SELECT COUNT(e) FROM EmotionRecord e WHERE e.userId = :userId " +
           "AND e.createdAt >= :startDate AND e.createdAt < :endDate")
    Long countByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}


