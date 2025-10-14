package likelion.harullala.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Boolean isOnboardingNeeded;

    public static AuthResponse of(String accessToken, String refreshToken, Boolean isOnboardingNeeded) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isOnboardingNeeded(isOnboardingNeeded)
                .build();
    }
}
