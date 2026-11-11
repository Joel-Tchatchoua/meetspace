package com.accenture.meetspace.user.auth.domain.vo;

import com.accenture.meetspace.user.auth.domain.enums.LoginStatus;
import com.accenture.meetspace.user.profile.application.port.out.identity.IdentityProvider;

public record Auth(String username, LoginStatus status, String accessToken, String refreshToken, String message) {

    public static Auth create(IdentityProvider identityProvider, String username, String password) {
        return identityProvider.exchangeCredentials(username, password)
                .map(tokens -> new Auth(username, LoginStatus.AUTHENTICATED, tokens.accessToken(),
                        tokens.refreshToken(), "Authentication successful"))
                .orElseGet(() -> new Auth(username, LoginStatus.INVALID_CREDENTIALS, null, null,
                        "Invalid username or password"));
    }

    public boolean isAuthenticated() {
        return status == LoginStatus.AUTHENTICATED;
    }

    @Override
    public String toString() {
        return "Auth[username=" + username + ", status=" + status + "]";
    }
}
