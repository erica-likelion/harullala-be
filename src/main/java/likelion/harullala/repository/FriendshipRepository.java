package likelion.harullala.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import likelion.harullala.domain.Friendship;
import likelion.harullala.domain.User;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /**
     * 특정 사용자 간의 친구 관계 조회
     */
    @Query("SELECT f FROM Friendship f WHERE (f.user1 = :user1 AND f.user2 = :user2) OR (f.user1 = :user2 AND f.user2 = :user1)")
    Optional<Friendship> findByUsers(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * 특정 사용자의 친구 목록 조회 (최대 5명)
     */
    @Query("SELECT f FROM Friendship f WHERE f.user1 = :user OR f.user2 = :user ORDER BY f.createdAt DESC")
    List<Friendship> findFriendsByUser(@Param("user") User user);

    /**
     * 특정 사용자의 친구 목록 조회 (제한된 수)
     */
    @Query("SELECT f FROM Friendship f WHERE f.user1 = :user OR f.user2 = :user ORDER BY f.createdAt DESC")
    List<Friendship> findFriendsByUserWithLimit(@Param("user") User user, @Param("limit") int limit);

    /**
     * 특정 사용자 간에 친구 관계가 존재하는지 확인
     */
    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE (f.user1 = :user1 AND f.user2 = :user2) OR (f.user1 = :user2 AND f.user2 = :user1)")
    boolean existsByUsers(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * 특정 사용자의 친구 수 조회
     */
    @Query("SELECT COUNT(f) FROM Friendship f WHERE f.user1 = :user OR f.user2 = :user")
    long countFriendsByUser(@Param("user") User user);
    
    /**
     * 특정 사용자 ID로 친구 관계 조회 (User 객체 대신 ID 사용)
     */
    @Query("SELECT f FROM Friendship f WHERE f.user1.id = :userId OR f.user2.id = :userId")
    List<Friendship> findByUser1IdOrUser2Id(@Param("userId") Long userId);
}
