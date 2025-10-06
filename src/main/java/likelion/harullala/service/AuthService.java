package likelion.harullala.service;

import likelion.harullala.dto.KakaoLoginReq;
import likelion.harullala.dto.AuthResponse;

public interface AuthService {
    AuthResponse kakaoLogin(KakaoLoginReq req);
}
