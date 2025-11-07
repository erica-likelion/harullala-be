package likelion.harullala.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import likelion.harullala.domain.EmotionRecord;
import likelion.harullala.domain.FeedReadStatus;
import likelion.harullala.domain.User;

public interface FeedReadStatusRepository extends JpaRepository<FeedReadStatus, Long> {

    boolean existsByReaderAndEmotionRecord(User reader, EmotionRecord emotionRecord);

    @Query("SELECT frs.emotionRecord.recordId FROM FeedReadStatus frs WHERE frs.reader.id = :readerId")
    List<Long> findReadRecordIdsByReaderId(@Param("readerId") Long readerId);

    long countByEmotionRecord(EmotionRecord emotionRecord);

    Optional<FeedReadStatus> findByReaderAndEmotionRecord(User reader, EmotionRecord emotionRecord);

    @Modifying
    @Query("DELETE FROM FeedReadStatus f WHERE f.reader.id = :readerId")
    void deleteAllByReader_Id(@Param("readerId") Long readerId);
}
