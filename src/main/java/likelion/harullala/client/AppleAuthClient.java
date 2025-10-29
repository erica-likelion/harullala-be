package likelion.harullala.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "apple-auth-api", url = "https://appleid.apple.com/auth")
public interface AppleAuthClient {

    @GetMapping("/keys")
    ApplePublicKeysResponse getPublicKeys();

    record ApplePublicKeysResponse(
            List<ApplePublicKey> keys
    ) {}

    record ApplePublicKey(
            String kty,
            String kid,
            String use,
            String alg,
            String n,
            String e
    ) {}
}
