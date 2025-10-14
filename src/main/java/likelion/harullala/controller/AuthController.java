package likelion.harullala.controller;

import likelion.harullala.dto.ApiSuccess;
import likelion.harullala.dto.TokenRefreshRequest;
import likelion.harullala.dto.TokenRefreshResponse;
import likelion.harullala.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/refresh")
    public ApiSuccess<TokenRefreshResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
        TokenRefreshResponse response = authService.refresh(request);
        return ApiSuccess.of(response, "토큰 재발급에 성공했습니다.");
    }
}
