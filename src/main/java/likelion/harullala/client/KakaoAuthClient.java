package likelion.harullala.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "kakao-auth-api", url = "https://kauth.kakao.com")
public interface KakaoAuthClient {

    @PostMapping("/oauth/token")
    KakaoTokenResponse getToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("code") String code
    );

    record KakaoTokenResponse(
            String token_type,
            String access_token,
            Integer expires_in,
            String refresh_token,
            Integer refresh_token_expires_in,
            String scope
    ) {}
}
