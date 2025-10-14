package likelion.harullala.repository;

import likelion.harullala.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderUserId(String providerUserId);
    Optional<User> findByRefreshToken(String refreshToken);
}
