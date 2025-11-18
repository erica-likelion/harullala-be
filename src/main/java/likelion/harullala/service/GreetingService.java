package likelion.harullala.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import likelion.harullala.domain.Character;
import likelion.harullala.domain.GreetingContext;
import likelion.harullala.domain.UserCharacter;
import likelion.harullala.dto.FriendInviteGreetingResponse;
import likelion.harullala.dto.GreetingResponse;
import likelion.harullala.dto.HomeGreetingResponse;
import likelion.harullala.repository.UserCharacterRepository;
import lombok.RequiredArgsConstructor;

/**
 * 통합 AI 인사말 서비스
 * 컨텍스트에 따라 적절한 서비스에 위임
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GreetingService {
    
    private final HomeGreetingService homeGreetingService;
    private final FriendInviteGreetingService friendInviteGreetingService;
    private final FriendReminderService friendReminderService;
    private final UserCharacterRepository userCharacterRepo;
    
    /**
     * 컨텍스트에 따른 AI 인사말 생성
     */
    public GreetingResponse generateGreeting(Long userId, GreetingContext context) {
        return switch (context) {
            case HOME -> generateHomeGreeting(userId);
            case FRIEND_INVITE -> generateFriendInviteGreeting(userId);
            case FRIEND_REMINDER -> generateFriendReminderGreeting(userId);
        };
    }
    
    /**
     * 홈 화면 인사말
     */
    private GreetingResponse generateHomeGreeting(Long userId) {
        HomeGreetingResponse response = homeGreetingService.generateGreeting(userId);
        
        return GreetingResponse.builder()
                .message(response.getMessage())
                .characterName(response.getCharacterName())
                .hasRecordedToday(response.getHasRecordedToday())
                .context(GreetingContext.HOME)
                .build();
    }
    
    /**
     * 친구 초대 페이지 멘트
     */
    private GreetingResponse generateFriendInviteGreeting(Long userId) {
        FriendInviteGreetingResponse response = friendInviteGreetingService.generateInviteGreeting(userId);
        
        return GreetingResponse.builder()
                .message(response.getMessage())
                .characterName(response.getCharacterName())
                .hasRecordedToday(null) // 이 컨텍스트에서는 불필요
                .context(GreetingContext.FRIEND_INVITE)
                .build();
    }
    
    /**
     * 친구 리마인더
     */
    private GreetingResponse generateFriendReminderGreeting(Long userId) {
        String message = friendReminderService.generateReminder(userId);
        
        // 현재 캐릭터 조회
        Character character = getCurrentCharacter(userId);
        String characterName = character != null ? character.getName() : "피코";
        
        return GreetingResponse.builder()
                .message(message)
                .characterName(characterName)
                .hasRecordedToday(null)
                .context(GreetingContext.FRIEND_REMINDER)
                .build();
    }
    
    /**
     * 현재 캐릭터 조회
     */
    private Character getCurrentCharacter(Long userId) {
        return userCharacterRepo.findByUserId(userId)
                .map(UserCharacter::getSelectedCharacter)
                .orElse(null);
    }
}

