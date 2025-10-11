package likelion.harullala.controller;

import likelion.harullala.config.security.CustomUserDetails;
import likelion.harullala.dto.ApiSuccess;
import likelion.harullala.dto.MyInfoResponse;
import likelion.harullala.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
