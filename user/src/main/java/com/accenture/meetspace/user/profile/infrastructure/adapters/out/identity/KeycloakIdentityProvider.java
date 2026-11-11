package com.accenture.meetspace.user.profile.infrastructure.adapters.out.identity;

import com.accenture.meetspace.user.shared.infrastructure.adapters.out.keycloak.KeycloakClient;
import com.accenture.meetspace.user.profile.application.port.out.identity.AuthTokens;
import com.accenture.meetspace.user.profile.application.port.out.identity.IdentityProvider;
import com.accenture.meetspace.user.profile.application.port.out.identity.IdentityUser;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;
import com.accenture.meetspace.user.profile.domain.exceptions.ExternalServiceUnavailableException;
import com.accenture.meetspace.user.profile.domain.exceptions.IdentityAlreadyExistsException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class KeycloakIdentityProvider implements IdentityProvider {

    private static final String INVALID_GRANT = "invalid_grant";
    private static final String USERS = "/users";
    private static final String USERS_BY_USERNAME = "/users?username={username}&exact=true";
    private static final String USER = "/users/{id}";
    private static final String ROLE = "/roles/{role}";
    private static final String ROLE_MAPPINGS = "/users/{id}/role-mappings/realm";
    private static final ParameterizedTypeReference<List<Map<String, Object>>> USERS_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<Map<String, Object>> ROLE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final KeycloakClient client;

    public KeycloakIdentityProvider(KeycloakClient client) {
        this.client = client;
    }

    @Override
    public UUID createUser(String username, String email, String password, String firstName, String lastName,
                           RoleEnum role) {
        var keycloakUserId = client.call(() -> {
            try {
                var location = client.admin(HttpMethod.POST, USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of(
                                "username", username,
                                "email", email,
                                "firstName", firstName,
                                "lastName", lastName,
                                "enabled", true,
                                "emailVerified", true,
                                "credentials", List.of(Map.of(
                                        "type", "password",
                                        "value", password,
                                        "temporary", false))))
                        .retrieve()
                        .toBodilessEntity()
                        .getHeaders()
                        .getLocation();
                return idOf(location);
            } catch (HttpClientErrorException.Conflict e) {
                throw new IdentityAlreadyExistsException(username);
            }
        });
        try {
            client.run(() -> addRole(keycloakUserId, role));
        } catch (ExternalServiceUnavailableException e) {
            deleteUser(keycloakUserId);
            throw e;
        }
        return keycloakUserId;
    }

    @Override
    public Optional<IdentityUser> findUser(String username) {
        return client.call(() -> client.admin(HttpMethod.GET, USERS_BY_USERNAME, username)
                .retrieve()
                .body(USERS_TYPE)
                .stream()
                .findFirst()
                .map(account -> new IdentityUser(
                        UUID.fromString((String) account.get("id")),
                        (String) account.get("username"),
                        (String) account.get("email"),
                        (String) account.get("firstName"),
                        (String) account.get("lastName"))));
    }

    @Override
    public void deleteUser(UUID keycloakUserId) {
        client.run(() -> {
            try {
                client.admin(HttpMethod.DELETE, USER, keycloakUserId)
                        .retrieve()
                        .toBodilessEntity();
            } catch (HttpClientErrorException.NotFound e) {
                return;
            }
        });
    }

    @Override
    public void changeRole(UUID keycloakUserId, RoleEnum role) {
        client.run(() -> {
            addRole(keycloakUserId, role);
            removeRole(keycloakUserId, role == RoleEnum.ADMIN ? RoleEnum.EMPLOYEE : RoleEnum.ADMIN);
        });
    }

    @Override
    public void enableUser(UUID keycloakUserId) {
        setEnabled(keycloakUserId, true);
    }

    @Override
    public void disableUser(UUID keycloakUserId) {
        setEnabled(keycloakUserId, false);
    }

    @Override
    public Optional<AuthTokens> exchangeCredentials(String username, String password) {
        return client.call(() -> {
            try {
                var tokens = client.passwordGrant(username, password);
                return Optional.of(new AuthTokens(tokens.accessToken(), tokens.refreshToken()));
            } catch (HttpClientErrorException e) {
                if (isRefusedGrant(e)) {
                    return Optional.empty();
                }
                throw e;
            }
        });
    }

    @Override
    public void revokeRefreshToken(String refreshToken) {
        client.run(() -> {
            try {
                client.logout(refreshToken);
            } catch (HttpClientErrorException e) {
                if (!isRefusedGrant(e)) {
                    throw e;
                }
            }
        });
    }

    private void setEnabled(UUID keycloakUserId, boolean enabled) {
        client.run(() -> client.admin(HttpMethod.PUT, USER, keycloakUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("enabled", enabled))
                .retrieve()
                .toBodilessEntity());
    }

    private void addRole(UUID keycloakUserId, RoleEnum role) {
        var representation = roleRepresentation(role);
        client.admin(HttpMethod.POST, ROLE_MAPPINGS, keycloakUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(representation))
                .retrieve()
                .toBodilessEntity();
    }

    private void removeRole(UUID keycloakUserId, RoleEnum role) {
        var representation = roleRepresentation(role);
        client.admin(HttpMethod.DELETE, ROLE_MAPPINGS, keycloakUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(representation))
                .retrieve()
                .toBodilessEntity();
    }

    private Map<String, Object> roleRepresentation(RoleEnum role) {
        return client.admin(HttpMethod.GET, ROLE, role.name())
                .retrieve()
                .body(ROLE_TYPE);
    }

    private static boolean isRefusedGrant(HttpClientErrorException exception) {
        return (exception.getStatusCode() == HttpStatus.BAD_REQUEST
                || exception.getStatusCode() == HttpStatus.UNAUTHORIZED)
                && exception.getResponseBodyAsString().contains(INVALID_GRANT);
    }

    private static UUID idOf(URI location) {
        var path = location.getPath();
        return UUID.fromString(path.substring(path.lastIndexOf('/') + 1));
    }
}
