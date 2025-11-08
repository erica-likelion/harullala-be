package likelion.harullala.service;

import likelion.harullala.domain.EmotionRecord;
import likelion.harullala.dto.EmotionCreateRequest;
import likelion.harullala.dto.EmotionDeleteResponse;
import likelion.harullala.dto.EmotionListResponse;
import likelion.harullala.dto.EmotionResponse;
import likelion.harullala.dto.EmotionUpdateRequest;
import likelion.harullala.dto.EmotionUpdateResponse;
import likelion.harullala.exception.EmotionRecordNotFoundException;
import likelion.harullala.exception.ForbiddenAccessException;
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
        // 감정 기록 엔티티 생성 (색상, 좌표, 감정명 포함)
        EmotionRecord emotionRecord = EmotionRecord.builder()
                .userId(userId)
                .record(request.getRecord())
                .emojiEmotion(request.getEmoji_emotion())
                .emotionName(request.getEmotion_name())
                .mainColor(request.getMain_color())
                .subColor(request.getSub_color())
                .textColor(request.getText_color()) // 텍스트 색상 추가
                .positionX(request.getPosition_x())
                .positionY(request.getPosition_y())
                .isShared(request.getIs_shared()) // 사용자가 선택한 공유 여부
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
        
        // 사용자의 감정기록 조회 (최신순)
        Page<EmotionRecord> emotionRecords = emotionRecordRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        // DTO로 변환
        return emotionRecords.getContent().stream()
                .map(EmotionListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 감정기록 단일 조회
     */
    public EmotionResponse getEmotionRecord(Long userId, Long recordId) {
        // 감정기록 조회
        EmotionRecord emotionRecord = emotionRecordRepository.findById(recordId)
                .orElseThrow(() -> new EmotionRecordNotFoundException("Emotion record not found"));

        // 권한 확인 - 본인의 감정기록인지 체크
        if (!emotionRecord.getUserId().equals(userId)) {
            throw new ForbiddenAccessException("You do not have permission");
        }

        // Response로 변환하여 반환
        return EmotionResponse.from(emotionRecord);
    }

    /**
     * 감정기록 수정
     */
    @Transactional
    public EmotionUpdateResponse updateEmotionRecord(Long userId, Long recordId, EmotionUpdateRequest request) {
        // 감정기록 조회
        EmotionRecord emotionRecord = emotionRecordRepository.findById(recordId)
                .orElseThrow(() -> new EmotionRecordNotFoundException("Emotion record not found"));

        // 권한 확인 - 본인의 감정기록인지 체크
        if (!emotionRecord.getUserId().equals(userId)) {
            throw new ForbiddenAccessException("You do not have permission to update this record");
        }

        // 감정기록 업데이트 (더티 체킹으로 자동 업데이트)
        // 기존 메서드 사용 - 텍스트만 업데이트하는 경우가 많으므로
        emotionRecord.updateRecord(request.getRecord());

        // Response로 변환하여 반환
        return EmotionUpdateResponse.from(emotionRecord);
    }

    /**
     * 감정기록 삭제
     */
    @Transactional
    public EmotionDeleteResponse deleteEmotionRecord(Long userId, Long recordId) {
        // 감정기록 조회
        EmotionRecord emotionRecord = emotionRecordRepository.findById(recordId)
                .orElseThrow(() -> new EmotionRecordNotFoundException("Emotion record not found"));

        // 권한 확인 - 본인의 감정기록인지 체크
        if (!emotionRecord.getUserId().equals(userId)) {
            throw new ForbiddenAccessException("You do not have permission");
        }

        // 실제 삭제 (Hard Delete)
        emotionRecordRepository.delete(emotionRecord);

        // Response로 변환하여 반환
        return EmotionDeleteResponse.of(recordId);
    }

    /**
     * 감정기록 공유 상태 변경
     */
    @Transactional
    public EmotionResponse updateSharedStatus(Long userId, Long recordId, Boolean isShared) {
        // 감정기록 조회
        EmotionRecord emotionRecord = emotionRecordRepository.findById(recordId)
                .orElseThrow(() -> new EmotionRecordNotFoundException("Emotion record not found"));

        // 권한 확인 - 본인의 감정기록인지 체크
        if (!emotionRecord.getUserId().equals(userId)) {
            throw new ForbiddenAccessException("You do not have permission");
        }

        // 공유 상태 변경 (더티 체킹으로 자동 업데이트)
        emotionRecord.updateSharedStatus(isShared);

        // Response로 변환하여 반환
        return EmotionResponse.from(emotionRecord);
    }

    /**
     * 공유된 감정기록 목록 조회 (페이지네이션)
     */
    public List<EmotionListResponse> getSharedEmotionRecordList(int page, int size) {
        // 페이지는 0부터 시작하므로 -1
        Pageable pageable = PageRequest.of(page - 1, size);
        
        // 공유된 감정기록 조회 (최신순)
        Page<EmotionRecord> emotionRecords = emotionRecordRepository
                .findByIsSharedTrueOrderByCreatedAtDesc(pageable);
        
        // DTO로 변환
        return emotionRecords.getContent().stream()
                .map(EmotionListResponse::from)
                .collect(Collectors.toList());
    }
}


