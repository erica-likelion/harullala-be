package likelion.harullala.service;

import likelion.harullala.dto.*;

public interface AuthService {
    AuthResponse kakaoLogin(KakaoLoginReq req);
    AuthResponse appleLogin(AppleLoginRequest req);
    TokenRefreshResponse refresh(TokenRefreshRequest request);
}
