package likelion.harullala.service;

import likelion.harullala.domain.EmotionRecord;
import likelion.harullala.dto.EmotionCreateRequest;
import likelion.harullala.dto.EmotionResponse;
import likelion.harullala.repository.EmotionRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmotionRecordService {

    private final EmotionRecordRepository emotionRecordRepository;

    @Transactional
    public EmotionResponse createEmotionRecord(Long userId, EmotionCreateRequest request) {
        // 감정 기록 엔티티 생성
        EmotionRecord emotionRecord = EmotionRecord.builder()
                .userId(userId)
                .record(request.getRecord())
                .emojiEmotion(request.getEmoji_emotion())
                .isDeleted(false)
                .build();

        // 저장
        EmotionRecord savedRecord = emotionRecordRepository.save(emotionRecord);

        // Response로 변환하여 반환
        return EmotionResponse.from(savedRecord);
    }
}


