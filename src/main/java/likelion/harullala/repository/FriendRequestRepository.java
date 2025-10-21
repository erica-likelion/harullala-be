package likelion.harullala.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import likelion.harullala.domain.FriendRequest;
import likelion.harullala.domain.FriendRequestStatus;
import likelion.harullala.domain.User;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    /**
     * 특정 사용자 간의 친구 요청 조회
     */
    Optional<FriendRequest> findByRequesterAndReceiver(User requester, User receiver);

    /**
     * 특정 사용자가 받은 친구 요청 목록 조회 (상태별)
     */
    List<FriendRequest> findByReceiverAndStatus(User receiver, FriendRequestStatus status);

    /**
     * 특정 사용자가 보낸 친구 요청 목록 조회 (상태별)
     */
    List<FriendRequest> findByRequesterAndStatus(User requester, FriendRequestStatus status);

    /**
     * 특정 사용자가 받은 대기 중인 친구 요청 목록 조회
     */
    @Query("SELECT fr FROM FriendRequest fr WHERE fr.receiver = :receiver AND fr.status = 'PENDING' ORDER BY fr.createdAt DESC")
    List<FriendRequest> findPendingRequestsByReceiver(@Param("receiver") User receiver);

    /**
     * 특정 사용자가 보낸 대기 중인 친구 요청 목록 조회
     */
    @Query("SELECT fr FROM FriendRequest fr WHERE fr.requester = :requester AND fr.status = 'PENDING' ORDER BY fr.createdAt DESC")
    List<FriendRequest> findPendingRequestsByRequester(@Param("requester") User requester);

    /**
     * 특정 사용자 간에 대기 중인 친구 요청이 있는지 확인
     */
    boolean existsByRequesterAndReceiverAndStatus(User requester, User receiver, FriendRequestStatus status);

    /**
     * 특정 사용자 간에 수락된 친구 요청이 있는지 확인
     */
    boolean existsByRequesterAndReceiverAndStatusIn(User requester, User receiver, List<FriendRequestStatus> statuses);
}
