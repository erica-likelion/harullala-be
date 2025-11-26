package likelion.harullala.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-Sent Events (SSE) 서비스
 * 실시간 이벤트 전송을 위한 SSE 연결 관리
 */
@Slf4j
@Service
public class SseService {

    // 사용자별 SSE 연결 저장 (userId -> SseEmitter)
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * SSE 연결 생성
     * @param userId 사용자 ID
     * @return SseEmitter
     */
    public SseEmitter createConnection(Long userId) {
        // 30분 타임아웃 설정
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        
        emitter.onCompletion(() -> {
            log.info("SSE 연결 종료: userId={}", userId);
            emitters.remove(userId);
        });
        
        emitter.onTimeout(() -> {
            log.info("SSE 연결 타임아웃: userId={}", userId);
            emitters.remove(userId);
            emitter.complete();
        });
        
        emitter.onError((ex) -> {
            log.error("SSE 연결 오류: userId={}, error={}", userId, ex.getMessage());
            emitters.remove(userId);
            emitter.completeWithError(ex);
        });
        
        emitters.put(userId, emitter);
        log.info("SSE 연결 생성: userId={}", userId);
        
        // 연결 확인 메시지 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE 연결이 성공적으로 설정되었습니다."));
        } catch (IOException e) {
            log.error("SSE 초기 메시지 전송 실패: userId={}", userId, e);
            emitters.remove(userId);
            emitter.completeWithError(e);
        }
        
        return emitter;
    }

    /**
     * 특정 사용자에게 이벤트 전송
     * @param userId 사용자 ID
     * @param eventName 이벤트 이름
     * @param data 이벤트 데이터
     */
    public void sendEvent(Long userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
                log.info("SSE 이벤트 전송 성공: userId={}, eventName={}", userId, eventName);
            } catch (IOException e) {
                log.error("SSE 이벤트 전송 실패: userId={}, eventName={}", userId, eventName, e);
                emitters.remove(userId);
                emitter.completeWithError(e);
            }
        } else {
            log.warn("SSE 연결이 없습니다: userId={}, eventName={}", userId, eventName);
        }
    }

    /**
     * 연결 종료
     * @param userId 사용자 ID
     */
    public void closeConnection(Long userId) {
        SseEmitter emitter = emitters.remove(userId);
        if (emitter != null) {
            emitter.complete();
            log.info("SSE 연결 종료: userId={}", userId);
        }
    }

    /**
     * 연결 여부 확인
     * @param userId 사용자 ID
     * @return 연결 여부
     */
    public boolean isConnected(Long userId) {
        return emitters.containsKey(userId);
    }
}

