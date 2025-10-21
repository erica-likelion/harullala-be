package likelion.harullala.config.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import likelion.harullala.util.JwtUtil;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            
            // 디버깅 로그 추가
            logger.info("JWT 토큰 추출: " + jwt);
            logger.info("요청 URL: " + request.getRequestURL());

            // 임시로 모든 요청을 인증 성공으로 처리
            if (request.getRequestURL().toString().contains("/api/v1/friends")) {
                logger.info("친구 API 요청 감지 - 임시 인증 성공 처리");
                UserDetails userDetails = customUserDetailsService.loadUserByUsername("1");
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("임시 인증 성공: userId 1");
            }

            // 테스트용 하드코딩된 토큰 처리
            if (StringUtils.hasText(jwt)) {
                Long userId = null;
                
                // 하드코딩된 테스트 토큰들
                if ("test-token-user-1".equals(jwt)) {
                    userId = 1L;
                    logger.info("하드코딩된 토큰 인식: test-token-user-1 -> userId: 1");
                } else if ("test-token-user-2".equals(jwt)) {
                    userId = 2L;
                    logger.info("하드코딩된 토큰 인식: test-token-user-2 -> userId: 2");
                } else if ("test-token-user-3".equals(jwt)) {
                    userId = 3L;
                    logger.info("하드코딩된 토큰 인식: test-token-user-3 -> userId: 3");
                } else if ("test-token-user-4".equals(jwt)) {
                    userId = 4L;
                    logger.info("하드코딩된 토큰 인식: test-token-user-4 -> userId: 4");
                } else if (jwtUtil.validateToken(jwt)) {
                    userId = jwtUtil.getUserIdFromToken(jwt);
                    logger.info("JWT 토큰 검증 성공: " + jwt + " -> userId: " + userId);
                } else {
                    logger.warn("알 수 없는 토큰: " + jwt);
                }

                if (userId != null) {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(String.valueOf(userId));
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("인증 성공: userId " + userId);
                }
            } else {
                logger.warn("JWT 토큰이 없음");
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.info("Authorization 헤더: " + bearerToken);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            logger.info("추출된 JWT 토큰: " + token);
            return token;
        }
        
        logger.warn("Authorization 헤더가 없거나 Bearer 형식이 아님: " + bearerToken);
        return null;
    }
}
