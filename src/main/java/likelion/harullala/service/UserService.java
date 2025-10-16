package likelion.harullala.service;

import likelion.harullala.dto.MyInfoResponse;
import likelion.harullala.dto.UpdateNicknameRequest;

public interface UserService {
    MyInfoResponse getMyInfo(Long userId);
    void updateCharacter(Long userId, Long newCharacterId);
    void updateNickname(Long userId, UpdateNicknameRequest request);
}
