package likelion.harullala.service.impl;

import likelion.harullala.domain.Character;
import likelion.harullala.domain.User;
import likelion.harullala.domain.UserCharacter;
import likelion.harullala.dto.UpdateNicknameRequest;
import likelion.harullala.repository.CharacterRepository;
import likelion.harullala.repository.UserCharacterRepository;
import likelion.harullala.repository.UserRepository;
import likelion.harullala.dto.CharacterInfo;
import likelion.harullala.dto.MyInfoResponse;
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

    @Override
    public MyInfoResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CharacterInfo characterInfo = userCharacterRepository.findByUserId(userId)
                .map(userCharacter -> CharacterInfo.from(userCharacter.getCharacter()))
                .orElse(null);

        return MyInfoResponse.of(user, characterInfo);
    }

    @Override
    @Transactional
    public void updateCharacter(Long userId, Long newCharacterId) {
        UserCharacter userCharacter = userCharacterRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("User has not selected a character yet."));

        Character newCharacter = characterRepository.findById(newCharacterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found"));

        userCharacter.updateCharacter(newCharacter);
    }

    @Override
    @Transactional
    public void updateNickname(Long userId, UpdateNicknameRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.updateNickname(request.getNickname());
    }
}
