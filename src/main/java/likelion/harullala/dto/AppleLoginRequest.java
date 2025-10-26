package likelion.harullala.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AppleLoginRequest(
        @JsonProperty("identity_token") String identityToken,
        @JsonProperty("authorization_code") String authorizationCode,
        String device,
        @JsonProperty("push_token") String pushToken
) {}
