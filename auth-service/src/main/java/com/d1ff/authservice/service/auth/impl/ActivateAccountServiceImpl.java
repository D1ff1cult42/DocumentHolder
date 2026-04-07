package com.d1ff.authservice.service.auth.impl;

import com.d1ff.authservice.entity.User;
import com.d1ff.authservice.entity.enums.WarningLevel;
import com.d1ff.authservice.repository.UserRepository;
import com.d1ff.authservice.service.auth.interfaces.ActivateAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ActivateAccountServiceImpl implements ActivateAccountService {
    private final UserRepository userRepository;

    @Value("${security.first-delition-warning}")
    private Duration firstDeletionWarning;

    @Value("${security.second-deletion-warning}")
    private Duration secondDeletionWarning;

    @Value("${security.account-deletion-time}")
    private Duration accountDeletionTime;

    @Override
    @Scheduled(fixedRate = 86400000)
    public void sendFirstDeletionWarning() {
        LocalDateTime threshold = LocalDateTime.now().minus(firstDeletionWarning);
        List<User> users = userRepository.findByWarningLevelAndCreatedAtBeforeAndVerifiedIsFalse(WarningLevel.NONE, threshold);

        //TODO на кафку users.forEach(user -> {})
    }

    @Override
    @Scheduled(fixedRate = 86400000)
    public void sendSecondDeletionWarning() {
        LocalDateTime threshold = LocalDateTime.now().minus(secondDeletionWarning);
        List<User> users = userRepository.findByWarningLevelAndCreatedAtBeforeAndVerifiedIsFalse(WarningLevel.FIRST, threshold);

        //TODO на кафку users.forEach(user -> {})
    }

    @Override
    @Scheduled(fixedRate = 86400000)
    @Transactional
    public void AccountDeletion() {
        LocalDateTime threshold = LocalDateTime.now().minus(accountDeletionTime);
        List<User> users = userRepository.findByCreatedAtBeforeAndVerifiedIsFalse(threshold);
        userRepository.deleteAll(users);
    }
}
