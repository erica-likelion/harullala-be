package likelion.harullala.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CachedMessage {
    private final String message;
    private final LocalDateTime cachedAt;
    
    public boolean isExpired() {
        return cachedAt.plusHours(24).isBefore(LocalDateTime.now());
    }
}

