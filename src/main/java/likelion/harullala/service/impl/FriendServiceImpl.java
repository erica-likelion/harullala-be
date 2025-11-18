package likelion.harullala.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import likelion.harullala.domain.FriendRelationship;
import likelion.harullala.domain.FriendStatus;
import likelion.harullala.domain.NotificationType;
import likelion.harullala.domain.User;
import likelion.harullala.dto.CancelFriendRequestDto;
import likelion.harullala.dto.FriendInfoDto;
import likelion.harullala.dto.ReceivedFriendRequestDto;
import likelion.harullala.dto.RemoveFriendDto;
import likelion.harullala.dto.RespondToFriendRequestDto;
import likelion.harullala.dto.SendFriendRequestDto;
import likelion.harullala.dto.SentFriendRequestDto;
import likelion.harullala.repository.EmotionRecordRepository;
import likelion.harullala.repository.FriendRelationshipRepository;
import likelion.harullala.repository.UserRepository;
import likelion.harullala.service.FriendService;
import likelion.harullala.service.NotificationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendServiceImpl implements FriendService {

    private final FriendRelationshipRepository friendRelationshipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmotionRecordRepository emotionRecordRepository;

    @Override
    @Transactional
    public void sendFriendRequest(Long requesterId, SendFriendRequestDto requestDto) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("요청자를 찾을 수 없습니다."));

        // connectCode로 친구 요청 (User 팀원이 findByConnectCode 메서드 추가 시 작동)
        User receiver = userRepository.findByConnectCode(requestDto.getConnectCode())
                .orElseThrow(() -> new IllegalArgumentException("해당 초대 코드를 가진 사용자를 찾을 수 없습니다."));

        // 자기 자신에게 요청하는 경우 방지
        if (requester.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }

        // 친구 수 제한 확인 (최대 5명)
        long currentFriendCount = friendRelationshipRepository.countAcceptedFriendsByUser(requester);
        if (currentFriendCount >= 5) {
            throw new IllegalArgumentException("친구가 너무 많습니다. 최대 5명까지만 추가할 수 있습니다.");
        }

        // 받는 사람의 친구 수도 확인
        long receiverFriendCount = friendRelationshipRepository.countAcceptedFriendsByUser(receiver);
        if (receiverFriendCount >= 5) {
            throw new IllegalArgumentException("상대방의 친구 목록이 가득 찼습니다.");
        }

        // 이미 친구인지 확인 (ACCEPTED 상태)
        if (friendRelationshipRepository.existsByUsersAndAccepted(requester, receiver)) {
            throw new IllegalArgumentException("이미 친구인 사용자입니다.");
        }

        // 이미 대기 중인 요청이 있는지 확인 (양방향 체크)
        if (friendRelationshipRepository.existsPendingRequestByRequester(requester, receiver)) {
            throw new IllegalArgumentException("이미 해당 사용자에게 친구 요청을 보냈습니다.");
        }
        
        if (friendRelationshipRepository.existsPendingRequestByRequester(receiver, requester)) {
            throw new IllegalArgumentException("해당 사용자가 이미 친구 요청을 보냈습니다. 받은 친구 요청을 확인해주세요.");
        }

        // 기존 관계가 있는지 확인하고 삭제 (PENDING이 아닌 경우)
        Optional<FriendRelationship> existingRelationship = friendRelationshipRepository.findByUsers(requester, receiver);
        if (existingRelationship.isPresent() && !existingRelationship.get().isPending()) {
            friendRelationshipRepository.delete(existingRelationship.get());
        }

        // user1 < user2 순서로 정규화
        User user1 = requester.getId() < receiver.getId() ? requester : receiver;
        User user2 = requester.getId() < receiver.getId() ? receiver : requester;

        // 친구 관계 생성 (PENDING 상태)
        FriendRelationship relationship = FriendRelationship.builder()
                .user1(user1)
                .user2(user2)
                .requester(requester)
                .status(FriendStatus.PENDING)
                .build();

        FriendRelationship saved = friendRelationshipRepository.save(relationship);
        
        // 받는 사람에게 푸시 알림 전송
        try {
            notificationService.sendNotification(
                receiver.getId(),
                NotificationType.FRIEND_REQUEST,
                "새로운 친구 요청이 도착했어요",
                requester.getNickname() + "님이 친구 요청을 보냈어요",
                saved.getId()
            );
        } catch (Exception e) {
            // 알림 전송 실패해도 친구 요청은 정상 처리
        }
    }

    @Override
    @Transactional
    public void respondToFriendRequest(Long receiverId, RespondToFriendRequestDto requestDto) {
        FriendRelationship relationship = friendRelationshipRepository.findById(requestDto.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다."));

        // 요청을 받는 사람이 맞는지 확인
        if (!relationship.isReceiver(receiverId)) {
            throw new IllegalArgumentException("해당 친구 요청에 응답할 권한이 없습니다.");
        }

        // 대기 중인 요청인지 확인
        if (!relationship.isPending()) {
            throw new IllegalArgumentException("이미 처리된 친구 요청입니다.");
        }

        if (requestDto.isAccept()) {
            // 친구 수 제한 확인 (수락하는 사람 기준)
            User receiver = userRepository.findById(receiverId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            long receiverFriendCount = friendRelationshipRepository.countAcceptedFriendsByUser(receiver);
            if (receiverFriendCount >= 5) {
                throw new IllegalArgumentException("친구가 너무 많습니다. 최대 5명까지만 추가할 수 있습니다.");
            }

            // 요청하는 사람의 친구 수도 확인
            long requesterFriendCount = friendRelationshipRepository.countAcceptedFriendsByUser(relationship.getRequester());
            if (requesterFriendCount >= 5) {
                throw new IllegalArgumentException("상대방의 친구 목록이 가득 찼습니다.");
            }

            // 친구 요청 수락 (상태를 ACCEPTED로 변경)
            relationship.accept();
            friendRelationshipRepository.save(relationship);
            
            // 요청한 사람에게 푸시 알림 전송
            try {
                User requester = relationship.getRequester();
                User accepter = receiver;
                notificationService.sendNotification(
                    requester.getId(),
                    NotificationType.FRIEND_ACCEPTED,
                    "친구 요청이 수락되었어요",
                    accepter.getNickname() + "님이 친구 요청을 수락했어요",
                    relationship.getId()
                );
            } catch (Exception e) {
                // 알림 전송 실패해도 친구 수락은 정상 처리
            }
        } else {
            // 친구 요청 거절 (상태를 REJECTED로 변경)
            relationship.reject();
            friendRelationshipRepository.save(relationship);
        }
    }

    @Override
    @Transactional
    public void cancelFriendRequest(Long requesterId, CancelFriendRequestDto requestDto) {
        FriendRelationship relationship = friendRelationshipRepository.findById(requestDto.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다."));

        // 요청을 보낸 사람이 맞는지 확인
        if (!relationship.isRequester(requesterId)) {
            throw new IllegalArgumentException("해당 친구 요청을 취소할 권한이 없습니다.");
        }

        // 대기 중인 요청인지 확인
        if (!relationship.isPending()) {
            throw new IllegalArgumentException("이미 처리된 친구 요청입니다.");
        }

        // 친구 요청 취소 (상태를 CANCELLED로 변경)
        relationship.cancel();
        friendRelationshipRepository.save(relationship);
    }

    @Override
    @Transactional
    public void removeFriend(Long userId, RemoveFriendDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        User friend = userRepository.findByConnectCode(requestDto.getConnectCode())
                .orElseThrow(() -> new IllegalArgumentException("해당 초대 코드를 가진 사용자를 찾을 수 없습니다."));

        // 친구 관계가 존재하는지 확인 (ACCEPTED 상태)
        FriendRelationship relationship = friendRelationshipRepository.findByUsersAndStatus(user, friend, FriendStatus.ACCEPTED)
                .orElseThrow(() -> new IllegalArgumentException("친구 관계가 존재하지 않습니다."));

        // 친구 관계 삭제 (물리적 삭제)
        friendRelationshipRepository.delete(relationship);
    }

    @Override
    public List<FriendInfoDto> getFriendsList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<FriendRelationship> relationships = friendRelationshipRepository.findAcceptedFriendsByUser(user);

        // 오늘 날짜 범위 계산
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return relationships.stream()
                .limit(5) // 최대 5명으로 제한
                .map(relationship -> {
                    User friend = relationship.getOtherUser(userId);
                    
                    // 오늘 기록 여부 확인
                    Long recordCount = emotionRecordRepository.countByUserIdAndDateRange(
                            friend.getId(), startOfDay, endOfDay);
                    Boolean hasRecordedToday = recordCount > 0;
                    
                    return new FriendInfoDto(
                            friend.getNickname(),
                            friend.getConnectCode(),
                            friend.getProfileImageUrl(),
                            hasRecordedToday
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ReceivedFriendRequestDto> getReceivedFriendRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<FriendRelationship> relationships = friendRelationshipRepository.findPendingRequestsAsReceiver(user);

        return relationships.stream()
                .map(relationship -> new ReceivedFriendRequestDto(
                        relationship.getId(),
                        relationship.getRequester().getNickname(),
                        relationship.getRequester().getConnectCode(),
                        relationship.getRequester().getProfileImageUrl(),
                        relationship.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<SentFriendRequestDto> getSentFriendRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<FriendRelationship> relationships = friendRelationshipRepository.findPendingRequestsAsRequester(user);

        return relationships.stream()
                .map(relationship -> {
                    User receiver = relationship.getOtherUser(userId);
                    return new SentFriendRequestDto(
                            relationship.getId(),
                            receiver.getNickname(),
                            receiver.getConnectCode(),
                            receiver.getProfileImageUrl(),
                            relationship.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }
}
