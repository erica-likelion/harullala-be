package likelion.harullala.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 홈 화면 AI 인사말 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeGreetingResponse {
    private String message;          // AI 인사말
    private Boolean hasRecordedToday; // 오늘 기록 작성 여부
    private String characterName;     // 캐릭터 이름
}

