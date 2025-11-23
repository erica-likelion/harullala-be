package likelion.harullala.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import likelion.harullala.config.security.CustomUserDetails;
import likelion.harullala.dto.ApiSuccess;
import likelion.harullala.dto.BlockFriendNotificationDto;
import likelion.harullala.dto.CancelFriendRequestDto;
import likelion.harullala.dto.FriendInfoDto;
import likelion.harullala.dto.ReceivedFriendRequestDto;
import likelion.harullala.dto.RemoveFriendDto;
import likelion.harullala.dto.RespondToFriendRequestDto;
import likelion.harullala.dto.SendFriendRequestDto;
import likelion.harullala.dto.SentFriendRequestDto;
import likelion.harullala.service.FriendService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/friends")
public class FriendController {

    private final FriendService friendService;

    /**
     * 친구 요청 보내기
     * POST /api/v1/friends/request
     */
    @PostMapping("/request")
    public ApiSuccess<?> sendFriendRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SendFriendRequestDto requestDto) {
        
        Long requesterId = userDetails.getUser().getId();
        friendService.sendFriendRequest(requesterId, requestDto);
        
        return ApiSuccess.of(null, "친구 요청이 성공적으로 전송되었습니다.");
    }

    /**
     * 친구 요청 수락/거절
     * PUT /api/v1/friends/request/respond
     */
    @PutMapping("/request/respond")
    public ApiSuccess<?> respondToFriendRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RespondToFriendRequestDto requestDto) {
        
        Long receiverId = userDetails.getUser().getId();
        friendService.respondToFriendRequest(receiverId, requestDto);
        
        String message = requestDto.isAccept() ? "친구 요청을 수락했습니다." : "친구 요청을 거절했습니다.";
        return ApiSuccess.of(null, message);
    }

    /**
     * 친구 요청 취소
     * DELETE /api/v1/friends/request/cancel
     */
    @DeleteMapping("/request/cancel")
    public ApiSuccess<?> cancelFriendRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CancelFriendRequestDto requestDto) {
        
        Long requesterId = userDetails.getUser().getId();
        friendService.cancelFriendRequest(requesterId, requestDto);
        
        return ApiSuccess.of(null, "친구 요청이 취소되었습니다.");
    }

    /**
     * 친구 해제
     * DELETE /api/v1/friends/remove
     */
    @DeleteMapping("/remove")
    public ApiSuccess<?> removeFriend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RemoveFriendDto requestDto) {
        
        Long userId = userDetails.getUser().getId();
        friendService.removeFriend(userId, requestDto);
        
        return ApiSuccess.of(null, "친구가 삭제되었습니다.");
    }

    /**
     * 친구 목록 조회 (최대 5명)
     * GET /api/v1/friends
     */
    @GetMapping
    public ApiSuccess<List<FriendInfoDto>> getFriendsList(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUser().getId();
        List<FriendInfoDto> friends = friendService.getFriendsList(userId);
        
        return ApiSuccess.of(friends, "친구 목록 조회가 성공했습니다.");
    }

    /**
     * 받은 친구 요청 목록 조회
     * GET /api/v1/friends/request/received
     */
    @GetMapping("/request/received")
    public ApiSuccess<List<ReceivedFriendRequestDto>> getReceivedFriendRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUser().getId();
        List<ReceivedFriendRequestDto> requests = friendService.getReceivedFriendRequests(userId);
        
        return ApiSuccess.of(requests, "받은 친구 요청 목록 조회가 성공했습니다.");
    }

    /**
     * 보낸 친구 요청 목록 조회
     * GET /api/v1/friends/request/sent
     */
    @GetMapping("/request/sent")
    public ApiSuccess<List<SentFriendRequestDto>> getSentFriendRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUser().getId();
        List<SentFriendRequestDto> requests = friendService.getSentFriendRequests(userId);
        
        return ApiSuccess.of(requests, "보낸 친구 요청 목록 조회가 성공했습니다.");
    }

    /**
     * 친구 푸시 알림 차단
     * POST /api/v1/friends/notification/block
     */
    @PostMapping("/notification/block")
    public ApiSuccess<?> blockFriendNotification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody BlockFriendNotificationDto requestDto) {
        
        Long userId = userDetails.getUser().getId();
        friendService.blockFriendNotification(userId, requestDto.getConnectCode());
        
        return ApiSuccess.of(null, "친구 푸시 알림이 차단되었습니다.");
    }

    /**
     * 친구 푸시 알림 차단 해제
     * DELETE /api/v1/friends/notification/unblock
     */
    @DeleteMapping("/notification/unblock")
    public ApiSuccess<?> unblockFriendNotification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody BlockFriendNotificationDto requestDto) {
        
        Long userId = userDetails.getUser().getId();
        friendService.unblockFriendNotification(userId, requestDto.getConnectCode());
        
        return ApiSuccess.of(null, "친구 푸시 알림 차단이 해제되었습니다.");
    }
}
