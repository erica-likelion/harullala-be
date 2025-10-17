package likelion.harullala.controller;

import likelion.harullala.config.security.CustomUserDetails;
import likelion.harullala.dto.ApiSuccess;
import likelion.harullala.dto.MyInfoResponse;
import likelion.harullala.dto.UpdateCharacterRequest;
import likelion.harullala.dto.UpdateNicknameRequest;
import likelion.harullala.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiSuccess<MyInfoResponse> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        MyInfoResponse myInfo = userService.getMyInfo(userId);

        return ApiSuccess.of(myInfo, "내 정보 조회가 성공했습니다.");
    }

    @PatchMapping("/me")
    public ApiSuccess<?> updateNickname(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UpdateNicknameRequest request) {
        Long userId = userDetails.getUser().getId();
        userService.updateNickname(userId, request);
        return ApiSuccess.of(null, "닉네임이 성공적으로 수정되었습니다.");
    }

    @PutMapping("/me/character")
    public ApiSuccess<?> updateCharacter(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UpdateCharacterRequest request) {
        Long userId = userDetails.getUser().getId();
        userService.updateCharacter(userId, request.getCharacterId());

        return ApiSuccess.of(null, "캐릭터가 성공적으로 수정되었습니다.");
    }
}
