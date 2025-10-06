package likelion.harullala.controller;

import likelion.harullala.dto.ApiSuccess;
import likelion.harullala.dto.AuthResponse;
import likelion.harullala.dto.KakaoLoginReq;
import likelion.harullala.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class KakaoLoginController {

    private final AuthService authService;

    @PostMapping("/kakao/login")
    public ApiSuccess<AuthResponse> kakaoLogin(@RequestBody KakaoLoginReq req) {
        return ApiSuccess.of(authService.kakaoLogin(req), "로그인 성공");
    }
}
