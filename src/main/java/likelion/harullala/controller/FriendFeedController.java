package likelion.harullala.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import likelion.harullala.config.security.CustomUserDetails;
import likelion.harullala.dto.ApiSuccess;
import likelion.harullala.dto.FriendFeedResponse;
import likelion.harullala.dto.MarkFeedReadRequest;
import likelion.harullala.service.FriendFeedService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/friend-feed")
public class FriendFeedController {

    private final FriendFeedService friendFeedService;

    /**
     * 친구들의 공유된 피드 조회 (24시간 이내)
     */
    @GetMapping
    public ApiSuccess<List<FriendFeedResponse>> getFriendFeeds(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long userId = userDetails.getUser().getId();
        List<FriendFeedResponse> feeds = friendFeedService.getFriendFeeds(userId, page, size);
        
        return ApiSuccess.of(feeds, "친구 피드 조회 성공");
    }

    /**
     * 친구 피드 상세 조회 (권한 검사 포함)
     */
    @GetMapping("/{recordId}")
    public ApiSuccess<FriendFeedResponse> getFriendFeedDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recordId) {
        
        Long userId = userDetails.getUser().getId();
        FriendFeedResponse feed = friendFeedService.getFriendFeedDetail(userId, recordId);
        
        return ApiSuccess.of(feed, "친구 피드 상세 조회 성공");
    }

    /**
     * 피드 읽음 처리
     */
    @PostMapping("/read")
    public ApiSuccess<Void> markFeedAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MarkFeedReadRequest request) {
        
        Long userId = userDetails.getUser().getId();
        friendFeedService.markFeedAsRead(userId, request);
        
        return ApiSuccess.of(null, "피드 읽음 처리 완료");
    }
}


