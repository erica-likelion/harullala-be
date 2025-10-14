package likelion.harullala.service;

import likelion.harullala.dto.KakaoLoginReq;
import likelion.harullala.dto.AuthResponse;
import likelion.harullala.dto.TokenRefreshRequest;
import likelion.harullala.dto.TokenRefreshResponse;

public interface AuthService {
    AuthResponse kakaoLogin(KakaoLoginReq req);
    TokenRefreshResponse refresh(TokenRefreshRequest request);
}
