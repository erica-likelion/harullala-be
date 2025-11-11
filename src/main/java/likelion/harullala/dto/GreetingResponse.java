package likelion.harullala.dto;

import likelion.harullala.domain.GreetingContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 통합 AI 인사말 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GreetingResponse {
    private String message;          // AI 인사말
    private String characterName;     // 캐릭터 이름
    private GreetingContext context;  // 컨텍스트 정보
    private Boolean hasRecordedToday; // 오늘 기록 여부 (home 컨텍스트일 때만)
}

