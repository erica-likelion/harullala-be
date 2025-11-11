package likelion.harullala.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import likelion.harullala.config.security.CustomUserDetails;
import likelion.harullala.domain.GreetingContext;
import likelion.harullala.dto.ApiSuccess;
import likelion.harullala.dto.GreetingResponse;
import likelion.harullala.service.GreetingService;
import lombok.RequiredArgsConstructor;

/**
 * 통합 AI 인사말 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class GreetingController {

    private final GreetingService greetingService;

    /**
     * AI 인사말 조회 (컨텍스트 기반)
     * GET /api/v1/greeting?context=home
     * GET /api/v1/greeting?context=friend-invite
     * GET /api/v1/greeting?context=friend-reminder
     */
    @GetMapping("/greeting")
    public ResponseEntity<ApiSuccess<GreetingResponse>> getGreeting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String context) {
        
        Long userId = userDetails.getUser().getId();
        
        // String을 GreetingContext로 변환 (유효하지 않으면 예외 발생)
        GreetingContext greetingContext = GreetingContext.fromValue(context);
        
        GreetingResponse response = greetingService.generateGreeting(userId, greetingContext);
        
        return ResponseEntity.ok(
            ApiSuccess.of(response, greetingContext.getDescription() + " 인사말을 생성했습니다.")
        );
    }
}

