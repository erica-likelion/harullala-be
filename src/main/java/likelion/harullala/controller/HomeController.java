package likelion.harullala.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import likelion.harullala.dto.ApiSuccess;
import likelion.harullala.dto.HomeGreetingResponse;
import likelion.harullala.service.HomeGreetingService;
import lombok.RequiredArgsConstructor;

/**
 * 홈 화면 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeGreetingService homeGreetingService;

    /**
     * 홈 화면 AI 인사말 조회
     * GET /api/v1/home/greeting
     */
    @GetMapping("/greeting")
    public ResponseEntity<ApiSuccess<HomeGreetingResponse>> getHomeGreeting() {
        Long userId = getCurrentUserId();
        HomeGreetingResponse response = homeGreetingService.generateGreeting(userId);
        
        return ResponseEntity.ok(ApiSuccess.of(response, "홈 인사말을 생성했습니다."));
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

