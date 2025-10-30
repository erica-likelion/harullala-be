package likelion.harullala.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import likelion.harullala.domain.FriendRelationship;
import likelion.harullala.domain.FriendStatus;
import likelion.harullala.domain.User;

@Repository
public interface FriendRelationshipRepository extends JpaRepository<FriendRelationship, Long> {

    @Query("SELECT fr FROM FriendRelationship fr WHERE (fr.user1 = :user1 AND fr.user2 = :user2) OR (fr.user1 = :user2 AND fr.user2 = :user1)")
    Optional<FriendRelationship> findByUsers(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT fr FROM FriendRelationship fr WHERE ((fr.user1 = :user1 AND fr.user2 = :user2) OR (fr.user1 = :user2 AND fr.user2 = :user1)) AND fr.status = :status")
    Optional<FriendRelationship> findByUsersAndStatus(@Param("user1") User user1, @Param("user2") User user2, @Param("status") FriendStatus status);

    @Query("SELECT COUNT(fr) > 0 FROM FriendRelationship fr WHERE ((fr.user1 = :user1 AND fr.user2 = :user2) OR (fr.user1 = :user2 AND fr.user2 = :user1)) AND fr.status = 'ACCEPTED'")
    boolean existsByUsersAndAccepted(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT COUNT(fr) > 0 FROM FriendRelationship fr WHERE fr.requester = :requester AND fr.status = 'PENDING' AND ((fr.user1 = :receiver) OR (fr.user2 = :receiver))")
    boolean existsPendingRequestByRequester(@Param("requester") User requester, @Param("receiver") User receiver);

    @Query("SELECT COUNT(fr) FROM FriendRelationship fr WHERE (fr.user1 = :user OR fr.user2 = :user) AND fr.status = 'ACCEPTED'")
    long countAcceptedFriendsByUser(@Param("user") User user);

    @Query("SELECT fr FROM FriendRelationship fr WHERE (fr.user1 = :user OR fr.user2 = :user) AND fr.status = 'ACCEPTED' ORDER BY fr.createdAt DESC")
    List<FriendRelationship> findAcceptedFriendsByUser(@Param("user") User user);

    @Query("SELECT fr FROM FriendRelationship fr WHERE fr.requester != :user AND (fr.user1 = :user OR fr.user2 = :user) AND fr.status = 'PENDING' ORDER BY fr.createdAt DESC")
    List<FriendRelationship> findPendingRequestsAsReceiver(@Param("user") User user);

    @Query("SELECT fr FROM FriendRelationship fr WHERE fr.requester = :user AND fr.status = 'PENDING' ORDER BY fr.createdAt DESC")
    List<FriendRelationship> findPendingRequestsAsRequester(@Param("user") User user);

    @Query("SELECT fr FROM FriendRelationship fr WHERE (fr.user1.id = :userId OR fr.user2.id = :userId) AND fr.status = 'ACCEPTED'")
    List<FriendRelationship> findAcceptedFriendsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM FriendRelationship fr WHERE fr.user1.id = :userId OR fr.user2.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
