package likelion.harullala.service;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import likelion.harullala.domain.AiFeedback;
import likelion.harullala.dto.CreateFeedbackRequest;
import likelion.harullala.dto.FeedbackDto;
import likelion.harullala.exception.ApiException;
import likelion.harullala.domain.NotificationType;
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
    private final NotificationService notificationService;

    public FeedbackDto createOrRegenerate(Long requester, CreateFeedbackRequest req) {

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

        String aiReply = chatGptClient.generateFeedback(rec.text(), rec.character());

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
        
        // 푸시 알림 전송
        try {
            notificationService.sendNotification(
                rec.userId(),
                NotificationType.AI_FEEDBACK,
                "AI 피드백이 도착했어요",
                "오늘의 감정에 대한 AI 피드백을 확인해보세요",
                saved.getFeedbackId()
            );
        } catch (Exception e) {
            // 알림 전송 실패해도 피드백 생성은 정상 처리
            // 로그는 NotificationService에서 기록됨
        }
        
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public FeedbackDto fetchByRecordId(Long requester, Long recordId) {

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