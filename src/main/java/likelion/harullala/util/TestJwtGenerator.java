package likelion.harullala.util;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class TestJwtGenerator {
    
    private static final String SECRET_KEY = "mySecretKey123456789012345678901234567890";
    private static final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    
    public String generateTestToken(Long userId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1시간
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    public static void main(String[] args) {
        TestJwtGenerator generator = new TestJwtGenerator();
        
        // 사용자 1용 토큰
        String token1 = generator.generateTestToken(1L);
        System.out.println("사용자 1 토큰: " + token1);
        
        // 사용자 2용 토큰
        String token2 = generator.generateTestToken(2L);
        System.out.println("사용자 2 토큰: " + token2);
    }
}
