package likelion.harullala.service.impl;

import likelion.harullala.client.KakaoAuthClient;
import likelion.harullala.client.KakaoApiClient;
import likelion.harullala.dto.AuthResponse;
import likelion.harullala.dto.KakaoLoginReq;
import likelion.harullala.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final KakaoAuthClient kakaoAuthClient;
    private final KakaoApiClient kakaoApiClient;

    @Value("${spring.kakao.api-key}")
    private String kakaoApiKey;

    @Override
    public AuthResponse kakaoLogin(KakaoLoginReq req) {
        KakaoAuthClient.KakaoTokenResponse tokenResponse = kakaoAuthClient.getToken(
                "authorization_code",
                kakaoApiKey,
                req.redirectUri(),
                req.authorizationCode()
        );

        KakaoApiClient.KakaoUserInfo userInfo = kakaoApiClient.getUserInfo("Bearer " + tokenResponse.access_token());

        // TODO: Find or create user
        // TODO: Create JWT token

        return new AuthResponse("dummy-jwt-token");
    }
}
