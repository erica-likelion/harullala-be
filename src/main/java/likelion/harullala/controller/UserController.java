package likelion.harullala.controller;

import likelion.harullala.dto.ApiSuccess;
import likelion.harullala.dto.MyInfoResponse;
import likelion.harullala.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiSuccess<MyInfoResponse> getMyInfo() {
        // TODO: 추후 Spring Security 도입 시, @AuthenticationPrincipal 어노테이션으로 실제 userId를 받아와야 합니다.
        Long tempUserId = 1L;

        MyInfoResponse myInfo = userService.getMyInfo(tempUserId);

        return ApiSuccess.of(myInfo, "내 정보 조회가 성공했습니다.");
    }
}
