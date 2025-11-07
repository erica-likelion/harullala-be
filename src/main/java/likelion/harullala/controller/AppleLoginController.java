package likelion.harullala.controller;

import likelion.harullala.dto.ApiSuccess;
import likelion.harullala.dto.AppleLoginRequest;
import likelion.harullala.dto.AuthResponse;
import likelion.harullala.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AppleLoginController {

    private final AuthService authService;

    @PostMapping("/apple/login")
    public ApiSuccess<AuthResponse> appleLogin(@RequestBody AppleLoginRequest request) {
        return ApiSuccess.of(authService.appleLogin(request), "로그인 성공");
    }
}
