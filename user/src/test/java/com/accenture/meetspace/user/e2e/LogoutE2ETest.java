package com.accenture.meetspace.user.e2e;

import com.accenture.meetspace.user.units.stubs.InMemoryIdentityProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@UserE2ETest
@DisplayName("POST /api/v1/auth/logout")
class LogoutE2ETest {

    @Autowired
    RestTestClient client;

    @Autowired
    InMemoryIdentityProvider identityProvider;

    @Test
    @DisplayName("revokes the refresh token")
    void revokesTheRefreshToken() {
        client.post()
                .uri("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("refreshToken", "refresh-to-revoke"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.revoked").isEqualTo(true);

        assertThat(identityProvider.revokedTokens()).contains("refresh-to-revoke");
    }

    @Test
    @DisplayName("rejects a missing refresh token with 400")
    void rejectsAMissingRefreshToken() {
        client.post()
                .uri("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0].field").isEqualTo("refreshToken");
    }
}
