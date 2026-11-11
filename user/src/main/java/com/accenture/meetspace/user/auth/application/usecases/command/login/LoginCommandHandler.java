package com.accenture.meetspace.user.auth.application.usecases.command.login;

import com.accenture.meetspace.bootstrap.application.command.CommandHandler;
import com.accenture.meetspace.user.auth.domain.exceptions.AccountDisabledException;
import com.accenture.meetspace.user.auth.domain.exceptions.InvalidCredentialsException;
import com.accenture.meetspace.user.auth.domain.vo.Auth;
import com.accenture.meetspace.user.profile.application.port.out.identity.IdentityProvider;
import com.accenture.meetspace.user.profile.application.port.out.repository.UserRepository;

public class LoginCommandHandler implements CommandHandler<LoginCommand, LoginResponse> {

    private final UserRepository userRepository;
    private final IdentityProvider identityProvider;

    public LoginCommandHandler(UserRepository userRepository, IdentityProvider identityProvider) {
        this.userRepository = userRepository;
        this.identityProvider = identityProvider;
    }

    @Override
    public LoginResponse handle(LoginCommand command) {
        var user = userRepository.findByUsername(command.username())
                .orElseThrow(InvalidCredentialsException::new);
        if (!user.isActive()) {
            throw new AccountDisabledException(command.username());
        }
        var auth = Auth.create(identityProvider, user.snapshot().username(), command.password());
        if (!auth.isAuthenticated()) {
            throw new InvalidCredentialsException();
        }
        return new LoginResponse(auth.accessToken(), auth.refreshToken(), auth.status(), auth.message());
    }
}
