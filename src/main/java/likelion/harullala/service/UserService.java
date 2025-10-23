package likelion.harullala.service;

import likelion.harullala.dto.MyInfoResponse;
import likelion.harullala.dto.UpdateNicknameRequest;
import likelion.harullala.dto.UpdateProfileImageRequest;

public interface UserService {
    MyInfoResponse getMyInfo(Long userId);
    void updateCharacter(Long userId, Long newCharacterId);
    void updateNickname(Long userId, UpdateNicknameRequest request);
    void updateProfileImage(Long userId, UpdateProfileImageRequest request);
}
