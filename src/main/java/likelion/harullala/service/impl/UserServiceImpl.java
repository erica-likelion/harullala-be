package likelion.harullala.service.impl;

import likelion.harullala.domain.User;
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

    @Override
    public MyInfoResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CharacterInfo characterInfo = userCharacterRepository.findByUserId(userId)
                .map(userCharacter -> CharacterInfo.from(userCharacter.getCharacter()))
                .orElse(null);

        return MyInfoResponse.of(user, characterInfo);
    }
}
