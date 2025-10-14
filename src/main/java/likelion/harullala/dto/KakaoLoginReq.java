package likelion.harullala.dto;

public record KakaoLoginReq (
        String authorizationCode,
        String redirectUri,
        String kakaoAccessToken,
        String device,
        String pushToken
){}
