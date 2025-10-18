package likelion.harullala.repository;

import likelion.harullala.domain.EmotionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}


