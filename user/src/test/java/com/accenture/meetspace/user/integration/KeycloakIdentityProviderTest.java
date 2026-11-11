package com.accenture.meetspace.user.integration;

import com.accenture.meetspace.user.shared.infrastructure.adapters.out.keycloak.KeycloakClient;
import com.accenture.meetspace.user.shared.infrastructure.config.KeycloakProperties;
import com.accenture.meetspace.user.profile.application.port.out.identity.AuthTokens;
import com.accenture.meetspace.user.profile.application.port.out.identity.IdentityUser;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;
import com.accenture.meetspace.user.profile.domain.exceptions.ExternalServiceUnavailableException;
import com.accenture.meetspace.user.profile.domain.exceptions.IdentityAlreadyExistsException;
import com.accenture.meetspace.user.profile.infrastructure.adapters.out.identity.KeycloakIdentityProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("KeycloakIdentityProvider")
class KeycloakIdentityProviderTest {

    private static final String TOKEN_PATH = "/realms/meetspace/protocol/openid-connect/token";
    private static final String LOGOUT_PATH = "/realms/meetspace/protocol/openid-connect/logout";
    private static final String USERS_PATH = "/admin/realms/meetspace/users";
    private static final String EMPLOYEE_ROLE_PATH = "/admin/realms/meetspace/roles/EMPLOYEE";
    private static final String ADMIN_ROLE_PATH = "/admin/realms/meetspace/roles/ADMIN";
    private static final String EMPLOYEE_ROLE = "{\"id\":\"role-employee\",\"name\":\"EMPLOYEE\"}";
    private static final String ADMIN_ROLE = "{\"id\":\"role-admin\",\"name\":\"ADMIN\"}";
    private static final UUID KEYCLOAK_USER_ID = UUID.randomUUID();
    private static final String USER_PATH = USERS_PATH + "/" + KEYCLOAK_USER_ID;
    private static final String ROLE_MAPPINGS_PATH = USER_PATH + "/role-mappings/realm";
    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    private FakeKeycloakServer keycloak;
    private KeycloakIdentityProvider provider;

    @BeforeEach
    void startKeycloak() throws IOException {
        keycloak = new FakeKeycloakServer();
        keycloak.respond("POST", TOKEN_PATH, 200, "{\"access_token\":\"service-token\"}");
        keycloak.respond("GET", EMPLOYEE_ROLE_PATH, 200, EMPLOYEE_ROLE);
        keycloak.respond("GET", ADMIN_ROLE_PATH, 200, ADMIN_ROLE);
        provider = new KeycloakIdentityProvider(new KeycloakClient(
                new KeycloakProperties(keycloak.url(), "meetspace", "backend", "s3cret", TIMEOUT, TIMEOUT)));
    }

    @AfterEach
    void stopKeycloak() {
        keycloak.close();
    }

    private UUID createJsmith() {
        return provider.createUser("jsmith", "jsmith@accenture.com", "Passw0rd!", "John", "Smith",
                RoleEnum.EMPLOYEE);
    }

    private void givenUserCreationSucceeds() {
        keycloak.respondCreated("POST", USERS_PATH, keycloak.url() + USER_PATH);
        keycloak.respond("POST", ROLE_MAPPINGS_PATH, 204, "");
    }

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("returns the id given by Keycloak in the location")
        void returnsTheIdGivenByKeycloak() {
            givenUserCreationSucceeds();

            assertThat(createJsmith()).isEqualTo(KEYCLOAK_USER_ID);
        }

        @Test
        @DisplayName("uses a service-account token")
        void usesAServiceAccountToken() {
            givenUserCreationSucceeds();

            createJsmith();

            assertThat(keycloak.requests("POST", TOKEN_PATH).getFirst().body())
                    .contains("grant_type=client_credentials")
                    .contains("client_id=backend")
                    .contains("client_secret=s3cret");
            assertThat(keycloak.requests("POST", USERS_PATH).getFirst().authorization())
                    .isEqualTo("Bearer service-token");
        }

        @Test
        @DisplayName("sends the user with a permanent password, enabled")
        void sendsTheUserWithAPermanentPassword() {
            givenUserCreationSucceeds();

            createJsmith();

            assertThat(keycloak.requests("POST", USERS_PATH).getFirst().body())
                    .contains("\"username\":\"jsmith\"")
                    .contains("\"email\":\"jsmith@accenture.com\"")
                    .contains("\"firstName\":\"John\"")
                    .contains("\"lastName\":\"Smith\"")
                    .contains("\"enabled\":true")
                    .contains("\"emailVerified\":true")
                    .contains("\"type\":\"password\"")
                    .contains("\"value\":\"Passw0rd!\"")
                    .contains("\"temporary\":false");
        }

        @Test
        @DisplayName("assigns the role")
        void assignsTheRole() {
            givenUserCreationSucceeds();

            createJsmith();

            assertThat(keycloak.requests("POST", ROLE_MAPPINGS_PATH).getFirst().body())
                    .contains("\"id\":\"role-employee\"")
                    .contains("\"name\":\"EMPLOYEE\"");
        }

        @Test
        @DisplayName("refuses a user who already exists in Keycloak")
        void refusesAUserWhoAlreadyExists() {
            keycloak.respond("POST", USERS_PATH, 409, "{\"errorMessage\":\"User exists with same username\"}");

            assertThatThrownBy(KeycloakIdentityProviderTest.this::createJsmith)
                    .isInstanceOf(IdentityAlreadyExistsException.class)
                    .hasFieldOrPropertyWithValue("code", "IDENTITY_ALREADY_EXISTS");
        }

        @Test
        @DisplayName("deletes the user again when the role cannot be assigned")
        void deletesTheUserWhenTheRoleCannotBeAssigned() {
            keycloak.respondCreated("POST", USERS_PATH, keycloak.url() + USER_PATH);
            keycloak.respond("POST", ROLE_MAPPINGS_PATH, 500, "");
            keycloak.respond("DELETE", USER_PATH, 204, "");

            assertThatThrownBy(KeycloakIdentityProviderTest.this::createJsmith)
                    .isInstanceOf(ExternalServiceUnavailableException.class);

            assertThat(keycloak.requests("DELETE", USER_PATH)).hasSize(1);
        }

        @Test
        @DisplayName("is unavailable when the service-account token is refused")
        void isUnavailableWhenTheServiceAccountTokenIsRefused() {
            keycloak.respond("POST", TOKEN_PATH, 401, "{\"error\":\"unauthorized_client\"}");

            assertThatThrownBy(KeycloakIdentityProviderTest.this::createJsmith)
                    .isInstanceOf(ExternalServiceUnavailableException.class);
            assertThat(keycloak.requests("POST", USERS_PATH)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findUser")
    class FindUser {

        @Test
        @DisplayName("returns the account with this exact username")
        void returnsTheAccountWithThisExactUsername() {
            keycloak.respond("GET", USERS_PATH, 200, """
                    [{"id": "%s", "username": "root", "email": "root@meetspace.local",
                      "firstName": "MeetSpace", "lastName": "Admin"}]
                    """.formatted(KEYCLOAK_USER_ID));

            var user = provider.findUser("root").orElseThrow();

            assertThat(user).isEqualTo(new IdentityUser(KEYCLOAK_USER_ID, "root", "root@meetspace.local",
                    "MeetSpace", "Admin"));
            var request = keycloak.requests("GET", USERS_PATH).getFirst();
            assertThat(request.query()).contains("username=root").contains("exact=true");
            assertThat(request.authorization()).isEqualTo("Bearer service-token");
        }

        @Test
        @DisplayName("returns nothing for an unknown username")
        void returnsNothingForAnUnknownUsername() {
            keycloak.respond("GET", USERS_PATH, 200, "[]");

            assertThat(provider.findUser("ghost")).isEmpty();
        }

        @Test
        @DisplayName("is unavailable when Keycloak fails")
        void isUnavailableWhenKeycloakFails() {
            keycloak.respond("GET", USERS_PATH, 500, "");

            assertThatThrownBy(() -> provider.findUser("root"))
                    .isInstanceOf(ExternalServiceUnavailableException.class);
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("deletes the user")
        void deletesTheUser() {
            keycloak.respond("DELETE", USER_PATH, 204, "");

            provider.deleteUser(KEYCLOAK_USER_ID);

            assertThat(keycloak.requests("DELETE", USER_PATH).getFirst().authorization())
                    .isEqualTo("Bearer service-token");
        }

        @Test
        @DisplayName("accepts a user who is already gone")
        void acceptsAUserWhoIsAlreadyGone() {
            keycloak.respond("DELETE", USER_PATH, 404, "");

            provider.deleteUser(KEYCLOAK_USER_ID);

            assertThat(keycloak.requests("DELETE", USER_PATH)).hasSize(1);
        }

        @Test
        @DisplayName("is unavailable when Keycloak fails")
        void isUnavailableWhenKeycloakFails() {
            keycloak.respond("DELETE", USER_PATH, 500, "");

            assertThatThrownBy(() -> provider.deleteUser(KEYCLOAK_USER_ID))
                    .isInstanceOf(ExternalServiceUnavailableException.class);
        }
    }

    @Nested
    @DisplayName("changeRole")
    class ChangeRole {

        @BeforeEach
        void givenRoleMappingsSucceed() {
            keycloak.respond("POST", ROLE_MAPPINGS_PATH, 204, "");
            keycloak.respond("DELETE", ROLE_MAPPINGS_PATH, 204, "");
        }

        @Test
        @DisplayName("to ADMIN adds ADMIN and removes EMPLOYEE")
        void toAdmin() {
            provider.changeRole(KEYCLOAK_USER_ID, RoleEnum.ADMIN);

            assertThat(keycloak.requests("POST", ROLE_MAPPINGS_PATH).getFirst().body()).contains("role-admin");
            assertThat(keycloak.requests("DELETE", ROLE_MAPPINGS_PATH).getFirst().body()).contains("role-employee");
        }

        @Test
        @DisplayName("to EMPLOYEE adds EMPLOYEE and removes ADMIN")
        void toEmployee() {
            provider.changeRole(KEYCLOAK_USER_ID, RoleEnum.EMPLOYEE);

            assertThat(keycloak.requests("POST", ROLE_MAPPINGS_PATH).getFirst().body()).contains("role-employee");
            assertThat(keycloak.requests("DELETE", ROLE_MAPPINGS_PATH).getFirst().body()).contains("role-admin");
        }
    }

    @Nested
    @DisplayName("status")
    class Status {

        @BeforeEach
        void givenUserUpdateSucceeds() {
            keycloak.respond("PUT", USER_PATH, 204, "");
        }

        @Test
        @DisplayName("disableUser disables the user")
        void disableUserDisablesTheUser() {
            provider.disableUser(KEYCLOAK_USER_ID);

            assertThat(keycloak.requests("PUT", USER_PATH).getFirst().body()).isEqualTo("{\"enabled\":false}");
        }

        @Test
        @DisplayName("enableUser enables the user")
        void enableUserEnablesTheUser() {
            provider.enableUser(KEYCLOAK_USER_ID);

            assertThat(keycloak.requests("PUT", USER_PATH).getFirst().body()).isEqualTo("{\"enabled\":true}");
        }
    }

    @Nested
    @DisplayName("exchange")
    class Exchange {

        @Test
        @DisplayName("returns the tokens of a password grant")
        void returnsTheTokensOfAPasswordGrant() {
            keycloak.respond("POST", TOKEN_PATH, 200, """
                    {"access_token": "access", "refresh_token": "refresh", "expires_in": 300}
                    """);

            var tokens = provider.exchangeCredentials("jsmith", "Passw0rd!");

            assertThat(tokens).contains(new AuthTokens("access", "refresh"));
            var request = keycloak.requests("POST", TOKEN_PATH).getFirst();
            assertThat(request.contentType()).startsWith("application/x-www-form-urlencoded");
            assertThat(request.body())
                    .contains("grant_type=password")
                    .contains("client_id=backend")
                    .contains("client_secret=s3cret")
                    .contains("username=jsmith")
                    .contains("password=Passw0rd%21");
        }

        @Test
        @DisplayName("returns nothing for a wrong password")
        void returnsNothingForAWrongPassword() {
            keycloak.respond("POST", TOKEN_PATH, 401, """
                    {"error": "invalid_grant", "error_description": "Invalid user credentials"}
                    """);

            assertThat(provider.exchangeCredentials("jsmith", "wrong")).isEmpty();
        }

        @Test
        @DisplayName("returns nothing for a grant Keycloak refuses with 400")
        void returnsNothingForAGrantKeycloakRefuses() {
            keycloak.respond("POST", TOKEN_PATH, 400, """
                    {"error": "invalid_grant", "error_description": "Account disabled"}
                    """);

            assertThat(provider.exchangeCredentials("jsmith", "Passw0rd!")).isEmpty();
        }

        @Test
        @DisplayName("is unavailable for a bad client configuration")
        void isUnavailableForABadClientConfiguration() {
            keycloak.respond("POST", TOKEN_PATH, 400, """
                    {"error": "unauthorized_client"}
                    """);

            assertThatThrownBy(() -> provider.exchangeCredentials("jsmith", "Passw0rd!"))
                    .isInstanceOf(ExternalServiceUnavailableException.class);
        }

        @Test
        @DisplayName("is unavailable when Keycloak fails")
        void isUnavailableWhenKeycloakFails() {
            keycloak.respond("POST", TOKEN_PATH, 500, "");

            assertThatThrownBy(() -> provider.exchangeCredentials("jsmith", "Passw0rd!"))
                    .isInstanceOf(ExternalServiceUnavailableException.class)
                    .hasFieldOrPropertyWithValue("code", "SERVICE_UNAVAILABLE");
        }

        @Test
        @DisplayName("is unavailable when Keycloak cannot be reached")
        void isUnavailableWhenKeycloakCannotBeReached() {
            keycloak.close();

            assertThatThrownBy(() -> provider.exchangeCredentials("jsmith", "Passw0rd!"))
                    .isInstanceOf(ExternalServiceUnavailableException.class);
        }
    }

    @Nested
    @DisplayName("revoke")
    class Revoke {

        @Test
        @DisplayName("sends the refresh token to the logout endpoint")
        void sendsTheRefreshTokenToTheLogoutEndpoint() {
            keycloak.respond("POST", LOGOUT_PATH, 204, "");

            provider.revokeRefreshToken("refresh");

            var request = keycloak.requests("POST", LOGOUT_PATH).getFirst();
            assertThat(request.body())
                    .contains("client_id=backend")
                    .contains("client_secret=s3cret")
                    .contains("refresh_token=refresh");
        }

        @Test
        @DisplayName("accepts a refresh token that is already invalid")
        void acceptsARefreshTokenThatIsAlreadyInvalid() {
            keycloak.respond("POST", LOGOUT_PATH, 400, """
                    {"error": "invalid_grant", "error_description": "Invalid refresh token"}
                    """);

            provider.revokeRefreshToken("expired");

            assertThat(keycloak.requests("POST", LOGOUT_PATH)).hasSize(1);
        }

        @Test
        @DisplayName("is unavailable when Keycloak fails")
        void isUnavailableWhenKeycloakFails() {
            keycloak.respond("POST", LOGOUT_PATH, 503, "");

            assertThatThrownBy(() -> provider.revokeRefreshToken("refresh"))
                    .isInstanceOf(ExternalServiceUnavailableException.class);
        }
    }

    @Test
    @DisplayName("is unavailable when Keycloak cannot be reached")
    void isUnavailableWhenKeycloakCannotBeReached() {
        keycloak.close();

        assertThatThrownBy(() -> provider.disableUser(KEYCLOAK_USER_ID))
                .isInstanceOf(ExternalServiceUnavailableException.class);
    }
}
