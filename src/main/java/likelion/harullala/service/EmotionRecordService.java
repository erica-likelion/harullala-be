package likelion.harullala.service;

import likelion.harullala.domain.EmotionRecord;
import likelion.harullala.dto.EmotionCreateRequest;
import likelion.harullala.dto.EmotionListResponse;
import likelion.harullala.dto.EmotionResponse;
import likelion.harullala.repository.EmotionRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * 감정기록 목록 조회 (페이지네이션)
     */
    public List<EmotionListResponse> getEmotionRecordList(Long userId, int page, int size) {
        // 페이지는 0부터 시작하므로 -1
        Pageable pageable = PageRequest.of(page - 1, size);
        
        // 사용자의 삭제되지 않은 감정기록 조회 (최신순)
        Page<EmotionRecord> emotionRecords = emotionRecordRepository
                .findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId, pageable);
        
        // DTO로 변환
        return emotionRecords.getContent().stream()
                .map(EmotionListResponse::from)
                .collect(Collectors.toList());
    }
}


