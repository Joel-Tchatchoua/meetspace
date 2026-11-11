package com.accenture.meetspace.user.auth.application.usecases.command.register;

import com.accenture.meetspace.bootstrap.application.command.CommandHandler;
import com.accenture.meetspace.user.auth.domain.exceptions.EmailAlreadyTakenException;
import com.accenture.meetspace.user.auth.domain.exceptions.UsernameAlreadyTakenException;
import com.accenture.meetspace.user.profile.application.port.out.identity.IdentityProvider;
import com.accenture.meetspace.user.profile.application.port.out.repository.UserRepository;
import com.accenture.meetspace.user.profile.domain.entities.Employee;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;

public class RegisterEmployeeCommandHandler
        implements CommandHandler<RegisterEmployeeCommand, RegisterEmployeeResponse> {

    private final UserRepository userRepository;
    private final IdentityProvider identityProvider;

    public RegisterEmployeeCommandHandler(UserRepository userRepository, IdentityProvider identityProvider) {
        this.userRepository = userRepository;
        this.identityProvider = identityProvider;
    }

    @Override
    public RegisterEmployeeResponse handle(RegisterEmployeeCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            throw new UsernameAlreadyTakenException(command.username());
        }
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyTakenException(command.email());
        }
        var keycloakUserId = identityProvider.createUser(command.username(), command.email(), command.password(),
                command.firstName(), command.lastName(), RoleEnum.EMPLOYEE);
        try {
            userRepository.save(Employee.create(command.userId(), keycloakUserId, command.username(),
                    command.email(), command.firstName(), command.lastName()));
        } catch (RuntimeException e) {
            identityProvider.deleteUser(keycloakUserId);
            throw e;
        }
        return new RegisterEmployeeResponse(command.userId());
    }
}
