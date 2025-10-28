package likelion.harullala.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReminderResponse {
    private String message;           // AI 메시지
    private int totalFriends;         // 총 친구 수
    private int recordedFriends;      // 오늘 기록한 친구 수
    private CharacterInfo character;  // 현재 캐릭터 정보
    private boolean hasUnrecorded;   // 미기록 친구 존재 여부
}


