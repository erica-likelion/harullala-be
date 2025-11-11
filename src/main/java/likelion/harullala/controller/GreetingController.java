package likelion.harullala.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestParam String context) {
        
        Long userId = getCurrentUserId();
        
        // String을 GreetingContext로 변환 (유효하지 않으면 예외 발생)
        GreetingContext greetingContext = GreetingContext.fromValue(context);
        
        GreetingResponse response = greetingService.generateGreeting(userId, greetingContext);
        
        return ResponseEntity.ok(
            ApiSuccess.of(response, greetingContext.getDescription() + " 인사말을 생성했습니다.")
        );
    }

    /**
     * 현재 인증된 사용자 ID 가져오기
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof likelion.harullala.config.security.CustomUserDetails) {
            likelion.harullala.config.security.CustomUserDetails userDetails = 
                (likelion.harullala.config.security.CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUser().getId();
        }
        throw new IllegalStateException("인증된 사용자를 찾을 수 없습니다.");
    }
}

