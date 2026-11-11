package com.accenture.meetspace.user.e2e;

import com.accenture.meetspace.user.profile.application.port.out.identity.AuthTokens;
import com.accenture.meetspace.user.units.stubs.InMemoryIdentityProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;

@UserE2ETest
@DisplayName("POST /api/v1/auth/login")
class LoginE2ETest {

    @Autowired
    RestTestClient client;

    @Autowired
    UserFixtures fixtures;

    @Autowired
    InMemoryIdentityProvider identityProvider;

    @BeforeEach
    void givenKeycloakKnowsJsmith() {
        identityProvider.accept("jsmith", UserFixtures.PASSWORD, new AuthTokens("access-token", "refresh-token"));
    }

    private RestTestClient.ResponseSpec login(String password) {
        return client.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", "jsmith", "password", password))
                .exchange();
    }

    @Test
    @DisplayName("returns the tokens of Keycloak")
    void returnsTheTokensOfKeycloak() {
        fixtures.registered("jsmith");

        login(UserFixtures.PASSWORD)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("access-token")
                .jsonPath("$.refreshToken").isEqualTo("refresh-token")
                .jsonPath("$.status").isEqualTo("AUTHENTICATED")
                .jsonPath("$.message").isEqualTo("Authentication successful");
    }

    @Test
    @DisplayName("rejects invalid credentials with 401")
    void rejectsInvalidCredentials() {
        fixtures.registered("jsmith");

        login("wrong")
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INVALID_CREDENTIALS")
                .jsonPath("$.service").isEqualTo("user");
    }

    @Test
    @DisplayName("rejects a deactivated user with 403")
    void rejectsADeactivatedUser() {
        var userId = fixtures.registered("jsmith");
        client.patch()
                .uri("/api/v1/users/{id}/status", userId)
                .header(HttpHeaders.AUTHORIZATION, fixtures.adminBearer("root"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("active", false))
                .exchange()
                .expectStatus().isOk();

        login(UserFixtures.PASSWORD)
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.code").isEqualTo("ACCOUNT_DISABLED");
    }

    @Test
    @DisplayName("rejects a missing password with 400")
    void rejectsAMissingPassword() {
        client.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", "jsmith"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0].field").isEqualTo("password");
    }
}
