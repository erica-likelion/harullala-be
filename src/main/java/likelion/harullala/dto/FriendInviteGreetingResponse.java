package likelion.harullala.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 친구 초대 페이지 AI 멘트 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendInviteGreetingResponse {
    private String message;       // AI 멘트
    private String characterName; // 캐릭터 이름
}

