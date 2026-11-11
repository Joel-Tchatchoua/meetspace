package com.accenture.meetspace.user.profile.infrastructure.adapters.out.identity;

import com.accenture.meetspace.user.profile.application.port.out.identity.AuthTokens;
import com.accenture.meetspace.user.profile.application.port.out.identity.IdentityProvider;
import com.accenture.meetspace.user.profile.application.port.out.identity.IdentityUser;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;
import com.accenture.meetspace.user.profile.domain.exceptions.ExternalServiceUnavailableException;

import java.util.Optional;
import java.util.UUID;

public class UnavailableIdentityProvider implements IdentityProvider {

    @Override
    public UUID createUser(String username, String email, String password, String firstName, String lastName,
                           RoleEnum role) {
        throw unavailable();
    }

    @Override
    public Optional<IdentityUser> findUser(String username) {
        throw unavailable();
    }

    @Override
    public void deleteUser(UUID keycloakUserId) {
        throw unavailable();
    }

    @Override
    public void changeRole(UUID keycloakUserId, RoleEnum role) {
        throw unavailable();
    }

    @Override
    public void enableUser(UUID keycloakUserId) {
        throw unavailable();
    }

    @Override
    public void disableUser(UUID keycloakUserId) {
        throw unavailable();
    }

    @Override
    public Optional<AuthTokens> exchangeCredentials(String username, String password) {
        throw unavailable();
    }

    @Override
    public void revokeRefreshToken(String refreshToken) {
        throw unavailable();
    }

    private static ExternalServiceUnavailableException unavailable() {
        return new ExternalServiceUnavailableException("Keycloak");
    }
}
