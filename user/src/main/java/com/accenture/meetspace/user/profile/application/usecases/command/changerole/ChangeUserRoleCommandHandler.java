package com.accenture.meetspace.user.profile.application.usecases.command.changerole;

import com.accenture.meetspace.bootstrap.application.command.CommandHandler;
import com.accenture.meetspace.user.profile.application.port.out.identity.IdentityProvider;
import com.accenture.meetspace.user.profile.application.port.out.repository.UserRepository;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;
import com.accenture.meetspace.user.profile.domain.exceptions.UserNotFoundException;

import java.util.UUID;

public class ChangeUserRoleCommandHandler implements CommandHandler<ChangeUserRoleCommand, ChangeUserRoleResponse> {

    private final UserRepository userRepository;
    private final IdentityProvider identityProvider;

    public ChangeUserRoleCommandHandler(UserRepository userRepository, IdentityProvider identityProvider) {
        this.userRepository = userRepository;
        this.identityProvider = identityProvider;
    }

    @Override
    public ChangeUserRoleResponse handle(ChangeUserRoleCommand command) {
        switch (command.role()) {
            case ADMIN -> promote(command.userId());
            case EMPLOYEE -> demote(command.userId());
        }
        return new ChangeUserRoleResponse(command.userId(), command.role());
    }

    private void promote(UUID userId) {
        if (userRepository.findAdmin(userId).isPresent()) {
            return;
        }
        var admin = userRepository.findEmployee(userId)
                .orElseThrow(() -> new UserNotFoundException(userId))
                .promote();
        userRepository.save(admin);
        identityProvider.changeRole(admin.snapshot().user().keycloakUserId(), RoleEnum.ADMIN);
    }

    private void demote(UUID userId) {
        if (userRepository.findEmployee(userId).isPresent()) {
            return;
        }
        var employee = userRepository.findAdmin(userId)
                .orElseThrow(() -> new UserNotFoundException(userId))
                .demote(userRepository.countActiveAdmins());
        userRepository.save(employee);
        identityProvider.changeRole(employee.snapshot().user().keycloakUserId(), RoleEnum.EMPLOYEE);
    }
}
