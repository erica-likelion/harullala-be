package likelion.harullala.dto;

public record KakaoLoginReq (
        String kakaoAccessToken,
        String device,
        String pushToken
){}
