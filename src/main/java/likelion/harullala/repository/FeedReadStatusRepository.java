package likelion.harullala.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import likelion.harullala.domain.EmotionRecord;
import likelion.harullala.domain.FeedReadStatus;
import likelion.harullala.domain.User;

public interface FeedReadStatusRepository extends JpaRepository<FeedReadStatus, Long> {

    /**
     * 특정 사용자가 특정 감정 기록을 읽었는지 확인
     */
    boolean existsByReaderAndEmotionRecord(User reader, EmotionRecord emotionRecord);

    /**
     * 특정 사용자가 읽은 감정 기록 ID 목록 조회
     */
    @Query("SELECT frs.emotionRecord.recordId FROM FeedReadStatus frs WHERE frs.reader.id = :readerId")
    List<Long> findReadRecordIdsByReaderId(@Param("readerId") Long readerId);

    /**
     * 특정 감정 기록을 읽은 사용자 수 조회
     */
    long countByEmotionRecord(EmotionRecord emotionRecord);

    /**
     * 특정 사용자의 읽음 상태 조회
     */
    Optional<FeedReadStatus> findByReaderAndEmotionRecord(User reader, EmotionRecord emotionRecord);
}
