package se.edugrade.winerater.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import se.edugrade.winerater.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
