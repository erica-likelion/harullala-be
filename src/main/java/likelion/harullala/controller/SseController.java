package likelion.harullala.controller;

import likelion.harullala.config.security.CustomUserDetails;
import likelion.harullala.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Server-Sent Events (SSE) 컨트롤러
 * 실시간 이벤트 스트림 제공
 */
@RestController
@RequestMapping("/api/v1/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;

    /**
     * SSE 연결 생성
     * GET /api/v1/sse/connect
     */
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        return sseService.createConnection(userId);
    }

    /**
     * SSE 연결 종료
     * DELETE /api/v1/sse/disconnect
     */
    @DeleteMapping("/disconnect")
    public void disconnect(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        sseService.closeConnection(userId);
    }
}

