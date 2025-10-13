package likelion.harullala.repository;

import likelion.harullala.domain.EmotionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmotionRecordRepository extends JpaRepository<EmotionRecord, Long> {
}


