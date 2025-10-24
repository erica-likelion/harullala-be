package likelion.harullala.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import likelion.harullala.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderUserId(String providerUserId);
    Optional<User> findByRefreshToken(String refreshToken);
    boolean existsByConnectCode(String connectCode);
    
    // TODO: User 팀에서 추가 필요 - 친구 요청 기능을 위해 필수
    Optional<User> findByConnectCode(String connectCode);
}
