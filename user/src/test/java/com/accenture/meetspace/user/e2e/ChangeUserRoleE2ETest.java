package com.accenture.meetspace.user.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;
import java.util.UUID;

@UserE2ETest
@DisplayName("PUT /api/v1/users/{id}/role")
class ChangeUserRoleE2ETest {

    @Autowired
    RestTestClient client;

    @Autowired
    UserFixtures fixtures;

    private RestTestClient.ResponseSpec changeRole(Object id, String role, String bearer) {
        return client.put()
                .uri("/api/v1/users/{id}/role", id)
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("role", role))
                .exchange();
    }

    @Test
    @DisplayName("promotes an employee to admin")
    void promotesAnEmployeeToAdmin() {
        var userId = fixtures.registered("jsmith");

        changeRole(userId, "ADMIN", fixtures.adminBearer("root"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userId").isEqualTo(userId.toString())
                .jsonPath("$.role").isEqualTo("ADMIN");

        client.get()
                .uri("/api/v1/users/{id}", userId)
                .header(HttpHeaders.AUTHORIZATION, fixtures.adminBearer("root"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.role").isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("demotes an admin when another active admin remains")
    void demotesAnAdminWhenAnotherActiveAdminRemains() {
        var other = fixtures.registered("jsmith");
        var rootBearer = fixtures.adminBearer("root");
        fixtures.adminId("root");
        changeRole(other, "ADMIN", rootBearer).expectStatus().isOk();

        changeRole(other, "EMPLOYEE", rootBearer)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.role").isEqualTo("EMPLOYEE");
    }

    @Nested
    @DisplayName("rejects")
    class Rejects {

        @Test
        @DisplayName("demoting the last active admin with 409")
        void demotingTheLastActiveAdmin() {
            var adminId = fixtures.adminId("root");

            changeRole(adminId, "EMPLOYEE", fixtures.adminBearer("root"))
                    .expectStatus().isEqualTo(409)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("LAST_ACTIVE_ADMIN");
        }

        @Test
        @DisplayName("an unknown user with 404")
        void anUnknownUser() {
            changeRole(UUID.randomUUID(), "ADMIN", fixtures.adminBearer("root"))
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("USER_NOT_FOUND");
        }

        @Test
        @DisplayName("an employee with 403")
        void anEmployee() {
            var userId = fixtures.registered("jsmith");

            changeRole(userId, "ADMIN", fixtures.employeeBearer("jsmith"))
                    .expectStatus().isForbidden()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("ACCESS_DENIED");
        }

        @Test
        @DisplayName("an unknown role with 400")
        void anUnknownRole() {
            changeRole(UUID.randomUUID(), "SERVICE", fixtures.adminBearer("root"))
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("MALFORMED_REQUEST");
        }
    }
}
