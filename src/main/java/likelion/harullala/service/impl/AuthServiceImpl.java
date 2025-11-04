package likelion.harullala.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
import likelion.harullala.client.AppleAuthClient;
import likelion.harullala.client.KakaoApiClient;
import likelion.harullala.client.KakaoAuthClient;
import likelion.harullala.domain.Provider;
import likelion.harullala.domain.User;
import likelion.harullala.dto.*;

import likelion.harullala.exception.ApiException;
import org.springframework.http.HttpStatus;
import likelion.harullala.repository.UserCharacterRepository;
import likelion.harullala.repository.UserRepository;
import likelion.harullala.service.AuthService;
import likelion.harullala.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final KakaoAuthClient kakaoAuthClient;
    private final KakaoApiClient kakaoApiClient;
    private final AppleAuthClient appleAuthClient;
    private final UserRepository userRepository;
    private final UserCharacterRepository userCharacterRepository;
    private final JwtUtil jwtUtil;

    @Value("${spring.kakao.api-key}")
    private String kakaoApiKey;

    @Value("${spring.apple.client-id}")
    private String appleClientId;

    @Override
    @Transactional
    public AuthResponse kakaoLogin(KakaoLoginReq req) {
        KakaoApiClient.KakaoUserInfo userInfo = kakaoApiClient.getUserInfo("Bearer " + req.kakaoAccessToken());

        User user = userRepository.findByProviderUserId(userInfo.id().toString())
                .orElseGet(() -> {
                    String connectCode;
                    do {
                        connectCode = generateConnectCode();
                    } while (userRepository.existsByConnectCode(connectCode));

                    User newUser = User.builder()
                            .email(userInfo.kakao_account() != null ? userInfo.kakao_account().email() : null)
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
    public AuthResponse appleLogin(AppleLoginRequest request) {
        Claims claims = verifyAndGetClaims(request.identityToken());
        String providerUserId = claims.getSubject();
        String email = claims.get("email", String.class);

        User user = userRepository.findByProviderUserId(providerUserId)
                .orElseGet(() -> {
                    String connectCode;
                    do {
                        connectCode = generateConnectCode();
                    } while (userRepository.existsByConnectCode(connectCode));

                    User newUser = User.builder()
                            .email(email)
                            .providerUserId(providerUserId)
                            .provider(Provider.APPLE)
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

    private String generateConnectCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int length = 8;
        java.util.Random random = new java.util.Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private Claims verifyAndGetClaims(String identityToken) {
        try {
            String headerOfIdentityToken = identityToken.substring(0, identityToken.indexOf("."));
            Map<String, String> header = new ObjectMapper().readValue(new String(Base64.getDecoder().decode(headerOfIdentityToken), StandardCharsets.UTF_8), Map.class);

            AppleAuthClient.ApplePublicKeysResponse publicKeys = appleAuthClient.getPublicKeys();
            AppleAuthClient.ApplePublicKey publicKey = publicKeys.keys().stream()
                    .filter(key -> key.kid().equals(header.get("kid")) && key.alg().equals(header.get("alg")))
                    .findFirst()
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "유효하지 않은 Apple 토큰입니다."));

            return getClaims(identityToken, publicKey);
        } catch (JsonProcessingException | ExpiredJwtException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "유효하지 않은 Apple 토큰입니다.", e);
        }
    }

    private Claims getClaims(String identityToken, AppleAuthClient.ApplePublicKey publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(publicKey.kty());
            BigInteger n = new BigInteger(1, Base64.getUrlDecoder().decode(publicKey.n()));
            BigInteger e = new BigInteger(1, Base64.getUrlDecoder().decode(publicKey.e()));
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
            PublicKey generatedPublicKey = keyFactory.generatePublic(publicKeySpec);

            return Jwts.parserBuilder()
                    .setSigningKey(generatedPublicKey)
                    .requireAudience(appleClientId)
                    .requireIssuer("https://appleid.apple.com")
                    .build()
                    .parseClaimsJws(identityToken)
                    .getBody();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "유효하지 않은 Apple 토큰입니다.", e);
        }
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

    @Override
    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.updateRefreshToken(null);
        userRepository.save(user);
    }
}


        