package likelion.harullala.service.impl;

import jakarta.transaction.Transactional;
import likelion.harullala.client.KakaoApiClient;
import likelion.harullala.client.KakaoAuthClient;
import likelion.harullala.domain.Provider;
import likelion.harullala.domain.User;
import likelion.harullala.repository.UserCharacterRepository;
import likelion.harullala.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final UserCharacterRepository userCharacterRepository;

    @Value("${spring.kakao.api-key}")
    private String kakaoApiKey;

    @Override
    @Transactional
    public AuthResponse kakaoLogin(KakaoLoginReq req) {
        KakaoAuthClient.KakaoTokenResponse tokenResponse = kakaoAuthClient.getToken(
                "authorization_code",
                kakaoApiKey,
                req.redirectUri(),
                req.authorizationCode()
        );

        KakaoApiClient.KakaoUserInfo userInfo = kakaoApiClient.getUserInfo("Bearer " + tokenResponse.access_token());

        User user = userRepository.findByProviderUserId(userInfo.id().toString())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .userName(userInfo.kakao_account().profile().nickname())
                            .providerUserId(userInfo.id().toString())
                            .provider(Provider.KAKAO)
                            .build();
                    return userRepository.save(newUser);
                });

        boolean isOnboardingNeeded = !userCharacterRepository.existsByUserId(user.getId());

        // TODO: Create JWT token
        String accessToken = "dummy-access-token";
        String refreshToken = "dummy-refresh-token";

        return AuthResponse.of(accessToken, refreshToken, isOnboardingNeeded);
    }
}
