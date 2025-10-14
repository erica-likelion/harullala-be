package likelion.harullala.service;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import likelion.harullala.domain.AiFeedback;
import likelion.harullala.dto.CreateFeedbackRequest;
import likelion.harullala.dto.FeedbackDto;
import likelion.harullala.exception.ApiException;
import likelion.harullala.infra.ChatGptClient;
import likelion.harullala.infra.RecordReader;
import likelion.harullala.repository.AiFeedbackRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AiFeedbackService {
    private static final int LIMIT = 3;

    private final AiFeedbackRepository feedbackRepo;
    private final RecordReader recordReader;
    private final ChatGptClient chatGptClient;

        // TODO: 실제 인증 시스템으로 교체 필요
        // 현재는 개발용으로 하드코딩된 사용자 ID 사용
        private Long currentUserId() {
            return 1L;
        }

    public FeedbackDto createOrRegenerate(CreateFeedbackRequest req) {
        Long requester = currentUserId();

        // 실제 테이블에서 데이터 가져오기
        var rec = recordReader.findActiveRecord(req.recordId());
        if (rec == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Record not found");
        }
        if (!rec.userId().equals(requester)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not your record");
        }

        AiFeedback f = feedbackRepo.findByRecordId(req.recordId()).orElse(null);
        int next = (f == null) ? 1 : f.getAttemptsUsed() + 1;
        if (next > LIMIT) {
            throw new ApiException(HttpStatus.CONFLICT, "Feedback limit exceeded (3/3)");
        }

        String aiReply = chatGptClient.generateFeedback(rec.text(), rec.emoji().name(), rec.character());

        if (f == null) {
            f = new AiFeedback();
            f.setRecordId(req.recordId());
            f.setUserId(rec.userId());
            f.setAttemptsUsed(1);
            f.setAiReply(aiReply);
            f.setCreatedAt(Instant.now());
            f.setUpdatedAt(Instant.now());
        } else {
            f.setAttemptsUsed(next);
            f.setAiReply(aiReply);
            f.setUpdatedAt(Instant.now());
        }
        AiFeedback saved = feedbackRepo.saveAndFlush(f);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public FeedbackDto fetchByRecordId(Long recordId) {
        Long requester = currentUserId();

        var rec = recordReader.findActiveRecord(recordId);
        if (rec == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Record not found");
        }
        if (!rec.userId().equals(requester)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not your record");
        }

        AiFeedback f = feedbackRepo.findByRecordId(recordId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Feedback not found"));
        return toDto(f);
    }

    private FeedbackDto toDto(AiFeedback f) {
        int used = f.getAttemptsUsed();
        return FeedbackDto.builder()
                .feedbackId(f.getFeedbackId())
                .recordId(f.getRecordId())
                .aiReply(f.getAiReply())
                .attempts(new FeedbackDto.Attempts(used, Math.max(0, LIMIT - used), LIMIT))
                .createdAt(f.getCreatedAt().toString())
                .updatedAt(f.getUpdatedAt().toString())
                .build();
    }
}