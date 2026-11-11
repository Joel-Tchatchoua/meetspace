package com.accenture.meetspace.user.profile.application.port.out.identity;

import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;

import java.util.Optional;
import java.util.UUID;

public interface IdentityProvider {

    UUID createUser(String username, String email, String password, String firstName, String lastName, RoleEnum role);

    Optional<IdentityUser> findUser(String username);

    void deleteUser(UUID keycloakUserId);

    void changeRole(UUID keycloakUserId, RoleEnum role);

    void enableUser(UUID keycloakUserId);

    void disableUser(UUID keycloakUserId);

    Optional<AuthTokens> exchangeCredentials(String username, String password);

    void revokeRefreshToken(String refreshToken);
}
