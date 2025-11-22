package likelion.harullala.repository;

import likelion.harullala.domain.FriendNotificationBlock;
import likelion.harullala.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 친구 푸시 알림 차단 Repository
 */
@Repository
public interface FriendNotificationBlockRepository extends JpaRepository<FriendNotificationBlock, Long> {

    /**
     * 사용자가 특정 친구를 차단했는지 확인
     */
    boolean existsByUserAndBlockedFriend(User user, User blockedFriend);

    /**
     * 사용자가 특정 친구를 차단한 정보 조회
     */
    Optional<FriendNotificationBlock> findByUserAndBlockedFriend(User user, User blockedFriend);

    @Modifying
    @Query("delete from FriendNotificationBlock f where f.user.id = :userId or f.blockedFriend.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}

