package com.accenture.meetspace.user.profile.application.usecases.command.createinitialadmin;

import com.accenture.meetspace.bootstrap.application.command.CommandHandler;
import com.accenture.meetspace.user.profile.application.port.out.identity.IdentityProvider;
import com.accenture.meetspace.user.profile.application.port.out.repository.UserRepository;
import com.accenture.meetspace.user.profile.domain.entities.Admin;
import com.accenture.meetspace.user.profile.domain.exceptions.InitialAdminNotFoundException;

import java.util.UUID;

public class CreateInitialAdminCommandHandler
        implements CommandHandler<CreateInitialAdminCommand, CreateInitialAdminResponse> {

    private final UserRepository userRepository;
    private final IdentityProvider identityProvider;

    public CreateInitialAdminCommandHandler(UserRepository userRepository, IdentityProvider identityProvider) {
        this.userRepository = userRepository;
        this.identityProvider = identityProvider;
    }

    @Override
    public CreateInitialAdminResponse handle(CreateInitialAdminCommand command) {
        var existing = userRepository.findByUsername(command.username());
        if (existing.isPresent()) {
            return new CreateInitialAdminResponse(existing.get().snapshot().id(), false);
        }
        var account = identityProvider.findUser(command.username())
                .orElseThrow(() -> new InitialAdminNotFoundException(command.username()));
        var userId = UUID.randomUUID();
        userRepository.save(Admin.create(userId, account.keycloakUserId(), account.username(), account.email(),
                account.firstName(), account.lastName()));
        return new CreateInitialAdminResponse(userId, true);
    }
}
