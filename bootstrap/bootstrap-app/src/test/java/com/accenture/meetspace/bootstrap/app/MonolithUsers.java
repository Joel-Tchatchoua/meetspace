package com.accenture.meetspace.bootstrap.app;

import com.accenture.meetspace.user.profile.application.port.out.repository.UserRepository;
import com.accenture.meetspace.user.profile.domain.entities.Admin;
import com.accenture.meetspace.user.profile.domain.entities.Employee;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

class MonolithUsers {

    private final UserRepository userRepository;
    private final TransactionTemplate transaction;

    MonolithUsers(UserRepository userRepository, PlatformTransactionManager transactionManager) {
        this.userRepository = userRepository;
        this.transaction = new TransactionTemplate(transactionManager);
    }

    void admin(String username) {
        transaction.executeWithoutResult(status -> userRepository.save(Admin.create(UUID.randomUUID(),
                UUID.randomUUID(), username, username + "@accenture.com", "Test", username)));
    }

    UUID employee(String username) {
        var id = UUID.randomUUID();
        transaction.executeWithoutResult(status -> userRepository.save(Employee.create(id, UUID.randomUUID(),
                username, username + "@accenture.com", "Test", username)));
        return id;
    }

    void deactivate(UUID userId) {
        transaction.executeWithoutResult(status -> {
            var user = userRepository.findUser(userId).orElseThrow();
            user.deactivate(UUID.randomUUID());
            userRepository.save(user);
        });
    }
}
