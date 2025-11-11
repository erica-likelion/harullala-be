package likelion.harullala.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import likelion.harullala.domain.Notification;
import likelion.harullala.domain.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 사용자의 알림 목록 조회 (페이징)
     */
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * 사용자의 특정 기간 내 알림 목록 조회 (페이징)
     */
    Page<Notification> findByUserAndCreatedAtAfterOrderByCreatedAtDesc(
        User user, LocalDateTime fromDate, Pageable pageable);

    /**
     * 사용자의 안읽은 알림 개수 조회
     */
    long countByUserAndIsReadFalse(User user);
}

