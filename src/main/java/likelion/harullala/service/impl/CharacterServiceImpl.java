package likelion.harullala.service.impl;

import jakarta.transaction.Transactional;
import likelion.harullala.domain.Character;
import likelion.harullala.domain.User;
import likelion.harullala.domain.UserCharacter;
import likelion.harullala.repository.CharacterRepository;
import likelion.harullala.repository.UserCharacterRepository;
import likelion.harullala.repository.UserRepository;
import likelion.harullala.service.CharacterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CharacterServiceImpl implements CharacterService {

    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;
    private final UserCharacterRepository userCharacterRepository;

    @Override
    public List<Character> getCharacterList() {
        return characterRepository.findAll();
    }

    @Override
    public void selectCharacter(Long userId, Long characterId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found"));

        if (userCharacterRepository.existsByUserId(userId)) {
            throw new IllegalStateException("User has already selected a character");
        }

        UserCharacter userCharacter = UserCharacter.builder()
                .user(user)
                .selectedCharacter(character)
                .active(true)  // 선택한 캐릭터는 활성화 상태로 설정
                .build();

        userCharacterRepository.save(userCharacter);
    }
}
