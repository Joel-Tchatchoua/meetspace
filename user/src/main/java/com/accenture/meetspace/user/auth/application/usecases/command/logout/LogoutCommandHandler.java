package com.accenture.meetspace.user.auth.application.usecases.command.logout;

import com.accenture.meetspace.bootstrap.application.command.CommandHandler;
import com.accenture.meetspace.user.profile.application.port.out.identity.IdentityProvider;

public class LogoutCommandHandler implements CommandHandler<LogoutCommand, LogoutResponse> {

    private final IdentityProvider identityProvider;

    public LogoutCommandHandler(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }

    @Override
    public LogoutResponse handle(LogoutCommand command) {
        identityProvider.revokeRefreshToken(command.refreshToken());
        return new LogoutResponse(true);
    }
}
