package com.accenture.meetspace.user.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UserE2ETest
@DisplayName("GET /api/v1/users/directory")
class SearchDirectoryE2ETest {

    @Autowired
    RestTestClient client;

    @Autowired
    UserFixtures fixtures;

    private UUID jdoe;

    @BeforeEach
    void givenColleagues() {
        fixtures.registered("jsmith", "John", "Smith");
        fixtures.registered("akumar", "Asha", "Kumar");
        jdoe = fixtures.registered("jdoe", "Jane", "Smithson");
    }

    private RestTestClient.ResponseSpec search(String name) {
        return client.get()
                .uri(uri -> uri.path("/api/v1/users/directory").queryParam("name", name).build())
                .header(HttpHeaders.AUTHORIZATION, fixtures.employeeBearer("jsmith"))
                .exchange();
    }

    @Test
    @DisplayName("finds colleagues by first or last name, in a page")
    void findsColleaguesByName() {
        search("smith")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[*].username").value(List.class, usernames ->
                        assertThat(usernames).containsExactly("jsmith", "jdoe"))
                .jsonPath("$.page.totalElements").isEqualTo(2);

        search("asha")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].username").isEqualTo("akumar");
    }

    @Test
    @DisplayName("returns only basic public fields")
    void returnsOnlyBasicPublicFields() {
        search("asha")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].userId").isNotEmpty()
                .jsonPath("$.content[0].firstName").isEqualTo("Asha")
                .jsonPath("$.content[0].lastName").isEqualTo("Kumar")
                .jsonPath("$.content[0].email").doesNotExist()
                .jsonPath("$.content[0].phone").doesNotExist()
                .jsonPath("$.content[0].role").doesNotExist();
    }

    @Test
    @DisplayName("leaves deactivated users out")
    void leavesDeactivatedUsersOut() {
        client.patch()
                .uri("/api/v1/users/{id}/status", jdoe)
                .header(HttpHeaders.AUTHORIZATION, fixtures.adminBearer("root"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("active", false))
                .exchange()
                .expectStatus().isOk();

        search("jane")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isEmpty();
    }

    @Test
    @DisplayName("rejects a missing name with 400")
    void rejectsAMissingName() {
        client.get()
                .uri("/api/v1/users/directory")
                .header(HttpHeaders.AUTHORIZATION, fixtures.employeeBearer("jsmith"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0].field").isEqualTo("name");
    }

    @Test
    @DisplayName("rejects a call without token with 401")
    void rejectsACallWithoutToken() {
        client.get()
                .uri(uri -> uri.path("/api/v1/users/directory").queryParam("name", "asha").build())
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("UNAUTHENTICATED");
    }
}
