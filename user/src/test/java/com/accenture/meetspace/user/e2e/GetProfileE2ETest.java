package com.accenture.meetspace.user.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.client.RestTestClient;

@UserE2ETest
@DisplayName("GET /api/v1/profile")
class GetProfileE2ETest {

    @Autowired
    RestTestClient client;

    @Autowired
    UserFixtures fixtures;

    private RestTestClient.ResponseSpec profile(String bearer) {
        return client.get().uri("/api/v1/profile").header(HttpHeaders.AUTHORIZATION, bearer).exchange();
    }

    @Test
    @DisplayName("shows my profile")
    void showsMyProfile() {
        var userId = fixtures.registered("jsmith");

        profile(fixtures.employeeBearer("jsmith"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(userId.toString())
                .jsonPath("$.username").isEqualTo("jsmith")
                .jsonPath("$.email").isEqualTo("jsmith@accenture.com")
                .jsonPath("$.firstName").isEqualTo("John")
                .jsonPath("$.lastName").isEqualTo("Smith")
                .jsonPath("$.role").isEqualTo("EMPLOYEE")
                .jsonPath("$.active").isEqualTo(true);
    }

    @Test
    @DisplayName("rejects a valid token without profile with 403")
    void rejectsAValidTokenWithoutProfile() {
        profile(fixtures.employeeBearer("ghost"))
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.code").isEqualTo("PROFILE_NOT_FOUND")
                .jsonPath("$.service").isEqualTo("meetspace");
    }

    @Nested
    @DisplayName("rejects with 401")
    class Unauthenticated {

        @Test
        @DisplayName("a call without token")
        void aCallWithoutToken() {
            client.get()
                    .uri("/api/v1/profile")
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("UNAUTHENTICATED")
                    .jsonPath("$.service").isEqualTo("user");
        }

        @Test
        @DisplayName("an expired token")
        void anExpiredToken() {
            profile(fixtures.expiredBearer("jsmith"))
                    .expectStatus().isUnauthorized()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("UNAUTHENTICATED")
                    .jsonPath("$.timestamp").isNotEmpty();
        }

        @Test
        @DisplayName("a token that is not valid")
        void aTokenThatIsNotValid() {
            profile("Bearer not-a-token")
                    .expectStatus().isUnauthorized()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("UNAUTHENTICATED");
        }
    }
}
