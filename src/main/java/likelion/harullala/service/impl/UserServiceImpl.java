package likelion.harullala.service.impl;

import likelion.harullala.domain.Character;
import likelion.harullala.domain.User;
import likelion.harullala.domain.UserCharacter;
import likelion.harullala.dto.CharacterInfo;
import likelion.harullala.dto.MyInfoResponse;
import likelion.harullala.dto.UpdateNicknameRequest;
import likelion.harullala.dto.UpdateProfileImageRequest;
import likelion.harullala.repository.*;
import likelion.harullala.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserCharacterRepository userCharacterRepository;
    private final CharacterRepository characterRepository;
    private final AiFeedbackRepository aiFeedbackRepository;
    private final EmotionRecordRepository emotionRecordRepository;
    private final FeedReadStatusRepository feedReadStatusRepository;
    private final FriendRelationshipRepository friendRelationshipRepository;
    private final NotificationRepository notificationRepository;
    private final FriendNotificationBlockRepository friendNotificationBlockRepository;


    @Override
    public MyInfoResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CharacterInfo characterInfo = userCharacterRepository.findByUserId(userId)
                .map(userCharacter -> CharacterInfo.from(userCharacter.getSelectedCharacter()))
                .orElse(null);

        return MyInfoResponse.of(user, characterInfo);
    }

    @Transactional
    public void updateCharacter(Long userId, Long newCharacterId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Character newCharacter = characterRepository.findById(newCharacterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found"));

        userCharacterRepository.findByUserId(userId)
                .ifPresentOrElse(
                        userCharacter -> {
                            // User already has a character, so update it.
                            if (!userCharacter.getIsActive()) {
                                userCharacter.setIsActive(true);
                            }
                            userCharacter.updateCharacter(newCharacter);
                        },
                        () -> {
                            // User does not have a character selected yet, so create it.
                            UserCharacter newUserCharacter = UserCharacter.builder()
                                    .user(user)
                                    .selectedCharacter(newCharacter)
                                    .build(); // isActive will be true by default because of @Builder.Default
                            userCharacterRepository.save(newUserCharacter);
                        }
                );
    }

    @Override
    @Transactional
    public void updateNickname(Long userId, UpdateNicknameRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.updateNickname(request.getNickname());
    }

    @Override
    @Transactional
    public void updateProfileImage(Long userId, UpdateProfileImageRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.updateProfileImageUrl(request.getProfileImageUrl());
    }

    @Override
    @Transactional
    public void withdraw(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Delete related data
        userCharacterRepository.deleteAllByUserId(userId);
        feedReadStatusRepository.deleteAllByReader_Id(userId);
        friendRelationshipRepository.deleteAllByUserId(userId);
        aiFeedbackRepository.deleteAllByUserId(userId);
        emotionRecordRepository.deleteAllByUserId(userId);
        notificationRepository.deleteAllByUserId(userId);
        friendNotificationBlockRepository.deleteAllByUserId(userId);

        // Delete the user
        userRepository.deleteById(userId);
    }
}
