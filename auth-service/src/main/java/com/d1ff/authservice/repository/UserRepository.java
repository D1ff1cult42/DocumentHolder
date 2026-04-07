package com.d1ff.authservice.repository;

import com.d1ff.authservice.entity.User;
import com.d1ff.authservice.entity.enums.WarningLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    List<User> findByWarningLevelAndCreatedAtBeforeAndVerifiedIsFalse(WarningLevel warningLevel, LocalDateTime threshold);

    List<User> findByCreatedAtBeforeAndVerifiedIsFalse(LocalDateTime threshold);
}
