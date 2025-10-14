package likelion.harullala.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "kakao-api", url = "https://kapi.kakao.com")
public interface KakaoApiClient {

    @PostMapping("/v2/user/me")
    KakaoUserInfo getUserInfo(@RequestHeader("Authorization") String accessToken);

    record KakaoUserInfo(
            Long id,
            String connected_at,
            KakaoAccount kakao_account
    ) {}

    record KakaoAccount(
            Profile profile,
            String email
    ) {}

    record Profile(
            String nickname,
            String profile_image_url,
            String thumbnail_image_url
    ) {}
}
