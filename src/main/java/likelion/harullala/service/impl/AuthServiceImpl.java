package likelion.harullala.service.impl;

import jakarta.transaction.Transactional;
import likelion.harullala.client.KakaoApiClient;
import likelion.harullala.client.KakaoAuthClient;
import likelion.harullala.domain.Provider;
import likelion.harullala.domain.User;
import likelion.harullala.dto.TokenRefreshRequest;
import likelion.harullala.dto.TokenRefreshResponse;
import likelion.harullala.repository.UserCharacterRepository;
import likelion.harullala.repository.UserRepository;
import likelion.harullala.dto.AuthResponse;
import likelion.harullala.dto.KakaoLoginReq;
import likelion.harullala.service.AuthService;
import likelion.harullala.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final KakaoAuthClient kakaoAuthClient;
    private final KakaoApiClient kakaoApiClient;
    private final UserRepository userRepository;
    private final UserCharacterRepository userCharacterRepository;
    private final JwtUtil jwtUtil;

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
                    String connectCode;
                    do {
                        connectCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                    } while (userRepository.existsByConnectCode(connectCode));

                    User newUser = User.builder()
                            .name(userInfo.kakao_account().profile().nickname())
                            .nickname(userInfo.kakao_account().profile().nickname())
                            .email(userInfo.kakao_account().email())
                            .providerUserId(userInfo.id().toString())
                            .provider(Provider.KAKAO)
                            .connectCode(connectCode)
                            .build();
                    return userRepository.save(newUser);
                });

        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        user.updateRefreshToken(refreshToken);
        userRepository.save(user);

        boolean isOnboardingNeeded = !userCharacterRepository.existsByUserId(user.getId());

        return AuthResponse.of(accessToken, refreshToken, isOnboardingNeeded);
    }

    @Override
    @Transactional
    public TokenRefreshResponse refresh(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new IllegalArgumentException("Refresh token mismatch");
        }

        String newAccessToken = jwtUtil.generateAccessToken(user.getId());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        user.updateRefreshToken(newRefreshToken);
        userRepository.save(user);

        return TokenRefreshResponse.of(newAccessToken, newRefreshToken);
    }
}
