package likelion.harullala.service;

import java.util.List;

import likelion.harullala.dto.CancelFriendRequestDto;
import likelion.harullala.dto.FriendInfoDto;
import likelion.harullala.dto.ReceivedFriendRequestDto;
import likelion.harullala.dto.RemoveFriendDto;
import likelion.harullala.dto.RespondToFriendRequestDto;
import likelion.harullala.dto.SendFriendRequestDto;
import likelion.harullala.dto.SentFriendRequestDto;

public interface FriendService {
    
    /**
     * 친구 요청 보내기 (초대코드 또는 userId로)
     */
    void sendFriendRequest(Long requesterId, SendFriendRequestDto requestDto);
    
    /**
     * 친구 요청 수락/거절
     */
    void respondToFriendRequest(Long receiverId, RespondToFriendRequestDto requestDto);
    
    /**
     * 친구 요청 취소 (보낸 사람이 대기 중 취소)
     */
    void cancelFriendRequest(Long requesterId, CancelFriendRequestDto requestDto);
    
    /**
     * 친구 해제 (이미 친구 관계 해제)
     */
    void removeFriend(Long userId, RemoveFriendDto requestDto);
    
    /**
     * 친구 목록 조회 (ACCEPTED만, 최대 5명)
     */
    List<FriendInfoDto> getFriendsList(Long userId);
    
    /**
     * 받은 친구 요청 목록 조회 (PENDING 상태)
     */
    List<ReceivedFriendRequestDto> getReceivedFriendRequests(Long userId);
    
    /**
     * 보낸 친구 요청 목록 조회 (PENDING 상태)
     */
    List<SentFriendRequestDto> getSentFriendRequests(Long userId);
    
    /**
     * 친구 푸시 알림 차단
     */
    void blockFriendNotification(Long userId, Long friendId);
    
    /**
     * 친구 푸시 알림 차단 해제
     */
    void unblockFriendNotification(Long userId, Long friendId);
}
