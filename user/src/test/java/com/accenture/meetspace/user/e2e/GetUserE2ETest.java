package com.accenture.meetspace.user.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.UUID;

@UserE2ETest
@DisplayName("GET /api/v1/users/{id}")
class GetUserE2ETest {

    @Autowired
    RestTestClient client;

    @Autowired
    UserFixtures fixtures;

    private RestTestClient.ResponseSpec getUser(Object id, String bearer) {
        return client.get()
                .uri("/api/v1/users/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .exchange();
    }

    @Test
    @DisplayName("returns one user to an admin")
    void returnsOneUserToAnAdmin() {
        var userId = fixtures.registered("jsmith");

        getUser(userId, fixtures.adminBearer("root"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(userId.toString())
                .jsonPath("$.username").isEqualTo("jsmith")
                .jsonPath("$.role").isEqualTo("EMPLOYEE");
    }

    @Test
    @DisplayName("rejects an unknown user with 404")
    void rejectsAnUnknownUser() {
        getUser(UUID.randomUUID(), fixtures.adminBearer("root"))
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("USER_NOT_FOUND");
    }

    @Test
    @DisplayName("rejects an id that is not a UUID with 400")
    void rejectsAnIdThatIsNotAUuid() {
        getUser("not-a-uuid", fixtures.adminBearer("root"))
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INVALID_REQUEST");
    }

    @Test
    @DisplayName("rejects an employee with 403")
    void rejectsAnEmployee() {
        var userId = fixtures.registered("jsmith");

        getUser(userId, fixtures.employeeBearer("jsmith"))
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.code").isEqualTo("ACCESS_DENIED");
    }
}
