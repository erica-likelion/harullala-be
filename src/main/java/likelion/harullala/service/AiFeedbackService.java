package likelion.harullala.service;


import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import likelion.harullala.domain.AiFeedback;
import likelion.harullala.domain.NotificationType;
import likelion.harullala.dto.CreateFeedbackRequest;
import likelion.harullala.dto.FeedbackDto;
import likelion.harullala.exception.ApiException;
import likelion.harullala.infra.ChatGptClient;
import likelion.harullala.infra.RecordReader;
import likelion.harullala.repository.AiFeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AiFeedbackService {
    private static final int LIMIT = 3;
    private static final int DELAY_MINUTES = 1;

    private final AiFeedbackRepository feedbackRepo;
    private final RecordReader recordReader;
    private final ChatGptClient chatGptClient;
    private final NotificationService notificationService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

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

        // 즉시 "처리 중" 응답 반환
        log.info("AI 피드백 생성 요청: recordId={}, userId={}, {}분 후 전송 예정", req.recordId(), requester, DELAY_MINUTES);
        
        // 1분 후에 백그라운드에서 비동기로 AI 답변 생성
        scheduler.schedule(() -> {
            try {
                generateAndSendFeedbackAsync(rec.recordId(), rec.userId(), rec.text(), rec.character(), next);
            } catch (Exception e) {
                log.error("AI 피드백 생성 및 전송 실패: recordId={}, error={}", rec.recordId(), e.getMessage(), e);
            }
        }, DELAY_MINUTES, TimeUnit.MINUTES);
        
        // 기존 피드백이 있으면 반환, 없으면 "처리 중" 응답 반환
        if (f != null) {
            return toDto(f);
        }
        
        // 피드백이 없으면 "처리 중" 응답 반환
        return new FeedbackDto(
                null,
                req.recordId(),
                null,
                next,
                null,
                null
        );
    }

    /**
     * AI 피드백 생성 및 알림 전송 (1분 후 비동기 처리)
     */
    @Transactional
    public void generateAndSendFeedbackAsync(Long recordId, Long userId, String text, likelion.harullala.domain.Character character, int attemptsUsed) {
        log.info("AI 피드백 생성 시작: recordId={}, userId={}", recordId, userId);
        
        try {
            // AI 답변 생성
            String aiReply = chatGptClient.generateFeedback(text, character);

            // 피드백 저장
            AiFeedback f = feedbackRepo.findByRecordId(recordId).orElse(null);
            if (f == null) {
                f = new AiFeedback();
                f.setRecordId(recordId);
                f.setUserId(userId);
                f.setAttemptsUsed(attemptsUsed);
                f.setAiReply(aiReply);
            } else {
                f.setAttemptsUsed(attemptsUsed);
                f.setAiReply(aiReply);
            }
            AiFeedback saved = feedbackRepo.saveAndFlush(f);
            
            log.info("AI 피드백 생성 완료: recordId={}, userId={}", recordId, userId);
            
            // 푸시 알림 전송
            try {
                notificationService.sendNotification(
                    userId,
                    NotificationType.AI_FEEDBACK,
                    "AI 피드백이 도착했어요",
                    "오늘의 감정에 대한 AI 피드백을 확인해보세요",
                    saved.getRecordId()  // recordId를 relatedId로 전달 (AI 피드백 조회 API가 recordId를 사용)
                );
                log.info("AI 피드백 푸시 알림 전송 완료: recordId={}, userId={}", recordId, userId);
            } catch (Exception e) {
                log.error("AI 피드백 푸시 알림 전송 실패: recordId={}, userId={}, error={}", recordId, userId, e.getMessage(), e);
            }
            
        } catch (Exception e) {
            log.error("AI 피드백 생성 실패: recordId={}, userId={}, error={}", recordId, userId, e.getMessage(), e);
        }
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
                .attemptsUsed(used)
                .createdAt(f.getCreatedAt().toString())
                .updatedAt(f.getUpdatedAt().toString())
                .build();
    }
}