package ru.netology.cloudservice.Repositories;

import ru.netology.cloudservice.Entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String login);
}
