package likelion.harullala.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import likelion.harullala.domain.FriendRequest;
import likelion.harullala.domain.FriendRequestStatus;
import likelion.harullala.domain.Friendship;
import likelion.harullala.domain.User;
import likelion.harullala.dto.CancelFriendRequestDto;
import likelion.harullala.dto.FriendInfoDto;
import likelion.harullala.dto.FriendRequestInfoDto;
import likelion.harullala.dto.RemoveFriendDto;
import likelion.harullala.dto.RespondToFriendRequestDto;
import likelion.harullala.dto.SendFriendRequestDto;
import likelion.harullala.repository.FriendRequestRepository;
import likelion.harullala.repository.FriendshipRepository;
import likelion.harullala.repository.UserRepository;
import likelion.harullala.service.FriendService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendServiceImpl implements FriendService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void sendFriendRequest(Long requesterId, SendFriendRequestDto requestDto) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("요청자를 찾을 수 없습니다."));

        User receiver;
        
        // userId로 친구 요청하는 경우만 지원 (connectCode는 유저 팀원이 추가 예정)
        if (requestDto.getUserId() != null) {
            receiver = userRepository.findById(requestDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("받는 사람을 찾을 수 없습니다."));
        } else {
            throw new IllegalArgumentException("사용자 ID를 입력해주세요.");
        }

        // 자기 자신에게 요청하는 경우 방지
        if (requester.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }

        // 친구 수 제한 확인 (최대 5명) - 먼저 확인
        long currentFriendCount = friendshipRepository.countFriendsByUser(requester);
        if (currentFriendCount >= 5) {
            throw new IllegalArgumentException("친구가 너무 많습니다. 최대 5명까지만 추가할 수 있습니다.");
        }

        // 받는 사람의 친구 수도 확인
        long receiverFriendCount = friendshipRepository.countFriendsByUser(receiver);
        if (receiverFriendCount >= 5) {
            throw new IllegalArgumentException("상대방의 친구 목록이 가득 찼습니다.");
        }

        // 이미 친구인지 확인
        if (friendshipRepository.existsByUsers(requester, receiver)) {
            throw new IllegalArgumentException("이미 친구인 사용자입니다.");
        }

        // 이미 대기 중인 요청이 있는지 확인 (양방향 체크)
        if (friendRequestRepository.existsByRequesterAndReceiverAndStatus(
                requester, receiver, FriendRequestStatus.PENDING)) {
            throw new IllegalArgumentException("이미 해당 사용자에게 친구 요청을 보냈습니다.");
        }
        
        if (friendRequestRepository.existsByRequesterAndReceiverAndStatus(
                receiver, requester, FriendRequestStatus.PENDING)) {
            throw new IllegalArgumentException("해당 사용자가 이미 친구 요청을 보냈습니다. 받은 친구 요청을 확인해주세요.");
        }
        
        // 기존 요청이 있는지 확인하고 삭제 (ACCEPTED, REJECTED, CANCELLED 상태만)
        Optional<FriendRequest> existingRequest = friendRequestRepository.findByRequesterAndReceiver(requester, receiver);
        if (existingRequest.isPresent() && existingRequest.get().getStatus() != FriendRequestStatus.PENDING) {
            friendRequestRepository.delete(existingRequest.get());
        }
        
        // 반대 방향 요청도 확인하고 삭제 (ACCEPTED, REJECTED, CANCELLED 상태만)
        Optional<FriendRequest> reverseRequest = friendRequestRepository.findByRequesterAndReceiver(receiver, requester);
        if (reverseRequest.isPresent() && reverseRequest.get().getStatus() != FriendRequestStatus.PENDING) {
            friendRequestRepository.delete(reverseRequest.get());
        }

        // 친구 요청 생성
        FriendRequest friendRequest = FriendRequest.builder()
                .requester(requester)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .build();

        friendRequestRepository.save(friendRequest);
    }

    @Override
    @Transactional
    public void respondToFriendRequest(Long receiverId, RespondToFriendRequestDto requestDto) {
        FriendRequest friendRequest = friendRequestRepository.findById(requestDto.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다."));

        // 요청을 받는 사람이 맞는지 확인
        if (!friendRequest.getReceiver().getId().equals(receiverId)) {
            throw new IllegalArgumentException("해당 친구 요청에 응답할 권한이 없습니다.");
        }

        // 대기 중인 요청인지 확인
        if (!friendRequest.isPending()) {
            throw new IllegalArgumentException("이미 처리된 친구 요청입니다.");
        }

        if (requestDto.isAccept()) {
            // 친구 수 제한 확인 (수락하는 사람 기준)
            long receiverFriendCount = friendshipRepository.countFriendsByUser(friendRequest.getReceiver());
            if (receiverFriendCount >= 5) {
                throw new IllegalArgumentException("친구가 너무 많습니다. 최대 5명까지만 추가할 수 있습니다.");
            }

            // 요청하는 사람의 친구 수도 확인
            long requesterFriendCount = friendshipRepository.countFriendsByUser(friendRequest.getRequester());
            if (requesterFriendCount >= 5) {
                throw new IllegalArgumentException("상대방의 친구 목록이 가득 찼습니다.");
            }

            // 친구 요청 수락
            friendRequest.accept();
            friendRequestRepository.save(friendRequest);

            // 친구 관계 생성 (user1 < user2 순서로 저장)
            User user1 = friendRequest.getRequester().getId() < friendRequest.getReceiver().getId() 
                    ? friendRequest.getRequester() : friendRequest.getReceiver();
            User user2 = friendRequest.getRequester().getId() < friendRequest.getReceiver().getId() 
                    ? friendRequest.getReceiver() : friendRequest.getRequester();

            Friendship friendship = Friendship.builder()
                    .user1(user1)
                    .user2(user2)
                    .build();

            friendshipRepository.save(friendship);
        } else {
            // 친구 요청 거절
            friendRequest.reject();
            friendRequestRepository.save(friendRequest);
        }
    }

    @Override
    @Transactional
    public void cancelFriendRequest(Long requesterId, CancelFriendRequestDto requestDto) {
        FriendRequest friendRequest = friendRequestRepository.findById(requestDto.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다."));

        // 요청을 보낸 사람이 맞는지 확인
        if (!friendRequest.getRequester().getId().equals(requesterId)) {
            throw new IllegalArgumentException("해당 친구 요청을 취소할 권한이 없습니다.");
        }

        // 대기 중인 요청인지 확인
        if (!friendRequest.isPending()) {
            throw new IllegalArgumentException("이미 처리된 친구 요청입니다.");
        }

        // 친구 요청 취소
        friendRequest.cancel();
        friendRequestRepository.save(friendRequest);
    }

    @Override
    @Transactional
    public void removeFriend(Long userId, RemoveFriendDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        User friend = userRepository.findById(requestDto.getFriendId())
                .orElseThrow(() -> new IllegalArgumentException("친구를 찾을 수 없습니다."));

        // 친구 관계가 존재하는지 확인
        Friendship friendship = friendshipRepository.findByUsers(user, friend)
                .orElseThrow(() -> new IllegalArgumentException("친구 관계가 존재하지 않습니다."));

        // 친구 관계 삭제
        friendshipRepository.delete(friendship);
    }

    @Override
    public List<FriendInfoDto> getFriendsList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Friendship> friendships = friendshipRepository.findFriendsByUser(user);

        return friendships.stream()
                .limit(5) // 최대 5명으로 제한
                .map(friendship -> {
                    User friend = friendship.getUser1().getId().equals(userId) 
                            ? friendship.getUser2() : friendship.getUser1();
                    return new FriendInfoDto(
                            friend.getId(),
                            friend.getNickname(),
                            friend.getConnectCode(),
                            friend.getName()
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendRequestInfoDto> getReceivedFriendRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<FriendRequest> requests = friendRequestRepository.findPendingRequestsByReceiver(user);

        return requests.stream()
                .map(request -> new FriendRequestInfoDto(
                        request.getId(),
                        request.getRequester().getId(),
                        request.getRequester().getNickname(),
                        request.getRequester().getConnectCode(),
                        request.getReceiver().getId(),
                        request.getReceiver().getNickname(),
                        request.getReceiver().getConnectCode(),
                        request.getStatus().name(),
                        request.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendRequestInfoDto> getSentFriendRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<FriendRequest> requests = friendRequestRepository.findPendingRequestsByRequester(user);

        return requests.stream()
                .map(request -> new FriendRequestInfoDto(
                        request.getId(),
                        request.getRequester().getId(),
                        request.getRequester().getNickname(),
                        request.getRequester().getConnectCode(),
                        request.getReceiver().getId(),
                        request.getReceiver().getNickname(),
                        request.getReceiver().getConnectCode(),
                        request.getStatus().name(),
                        request.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
