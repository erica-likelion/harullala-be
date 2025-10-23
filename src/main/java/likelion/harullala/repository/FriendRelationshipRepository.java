package likelion.harullala.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import likelion.harullala.domain.FriendRelationship;
import likelion.harullala.domain.FriendStatus;
import likelion.harullala.domain.User;

@Repository
public interface FriendRelationshipRepository extends JpaRepository<FriendRelationship, Long> {

    /**
     * 특정 사용자 간의 관계 조회 (모든 상태)
     */
    @Query("SELECT fr FROM FriendRelationship fr WHERE (fr.user1 = :user1 AND fr.user2 = :user2) OR (fr.user1 = :user2 AND fr.user2 = :user1)")
    Optional<FriendRelationship> findByUsers(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * 특정 사용자 간의 특정 상태 관계 조회
     */
    @Query("SELECT fr FROM FriendRelationship fr WHERE ((fr.user1 = :user1 AND fr.user2 = :user2) OR (fr.user1 = :user2 AND fr.user2 = :user1)) AND fr.status = :status")
    Optional<FriendRelationship> findByUsersAndStatus(@Param("user1") User user1, @Param("user2") User user2, @Param("status") FriendStatus status);

    /**
     * 친구 관계가 존재하는지 확인 (ACCEPTED 상태만)
     */
    @Query("SELECT COUNT(fr) > 0 FROM FriendRelationship fr WHERE ((fr.user1 = :user1 AND fr.user2 = :user2) OR (fr.user1 = :user2 AND fr.user2 = :user1)) AND fr.status = 'ACCEPTED'")
    boolean existsByUsersAndAccepted(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * 대기 중인 요청이 존재하는지 확인
     */
    @Query("SELECT COUNT(fr) > 0 FROM FriendRelationship fr WHERE fr.requester = :requester AND fr.status = 'PENDING' AND ((fr.user1 = :receiver) OR (fr.user2 = :receiver))")
    boolean existsPendingRequestByRequester(@Param("requester") User requester, @Param("receiver") User receiver);

    /**
     * 특정 사용자의 친구 수 조회 (ACCEPTED 상태만)
     */
    @Query("SELECT COUNT(fr) FROM FriendRelationship fr WHERE (fr.user1 = :user OR fr.user2 = :user) AND fr.status = 'ACCEPTED'")
    long countAcceptedFriendsByUser(@Param("user") User user);

    /**
     * 특정 사용자의 친구 목록 조회 (ACCEPTED 상태만)
     */
    @Query("SELECT fr FROM FriendRelationship fr WHERE (fr.user1 = :user OR fr.user2 = :user) AND fr.status = 'ACCEPTED' ORDER BY fr.createdAt DESC")
    List<FriendRelationship> findAcceptedFriendsByUser(@Param("user") User user);

    /**
     * 특정 사용자가 받은 대기 중인 요청 목록 조회
     */
    @Query("SELECT fr FROM FriendRelationship fr WHERE fr.requester != :user AND (fr.user1 = :user OR fr.user2 = :user) AND fr.status = 'PENDING' ORDER BY fr.createdAt DESC")
    List<FriendRelationship> findPendingRequestsAsReceiver(@Param("user") User user);

    /**
     * 특정 사용자가 보낸 대기 중인 요청 목록 조회
     */
    @Query("SELECT fr FROM FriendRelationship fr WHERE fr.requester = :user AND fr.status = 'PENDING' ORDER BY fr.createdAt DESC")
    List<FriendRelationship> findPendingRequestsAsRequester(@Param("user") User user);

    /**
     * 특정 사용자의 친구 관계 조회 (ACCEPTED 상태만, ID로 조회)
     */
    @Query("SELECT fr FROM FriendRelationship fr WHERE (fr.user1.id = :userId OR fr.user2.id = :userId) AND fr.status = 'ACCEPTED'")
    List<FriendRelationship> findAcceptedFriendsByUserId(@Param("userId") Long userId);
}

