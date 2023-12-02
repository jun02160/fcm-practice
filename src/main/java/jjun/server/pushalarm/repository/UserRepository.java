package jjun.server.pushalarm.repository;

import jjun.server.pushalarm.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
