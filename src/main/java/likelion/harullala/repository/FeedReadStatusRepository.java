package likelion.harullala.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import likelion.harullala.domain.FeedReadStatus;

public interface FeedReadStatusRepository extends JpaRepository<FeedReadStatus, Long> {

    /**
     * 특정 사용자가 특정 감정 기록을 읽었는지 확인
     */
    boolean existsByReaderIdAndRecordId(Long readerId, Long recordId);

    /**
     * 특정 사용자가 읽은 감정 기록 ID 목록 조회
     */
    @Query("SELECT frs.recordId FROM FeedReadStatus frs WHERE frs.readerId = :readerId")
    List<Long> findReadRecordIdsByReaderId(@Param("readerId") Long readerId);

    /**
     * 특정 감정 기록을 읽은 사용자 수 조회
     */
    long countByRecordId(Long recordId);

    /**
     * 특정 사용자의 읽음 상태 조회
     */
    Optional<FeedReadStatus> findByReaderIdAndRecordId(Long readerId, Long recordId);
}
