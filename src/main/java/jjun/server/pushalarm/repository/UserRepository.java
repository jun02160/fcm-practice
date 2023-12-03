package jjun.server.pushalarm.repository;

import java.util.Optional;
import jjun.server.pushalarm.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByFcmToken(String fcmToken);
}
