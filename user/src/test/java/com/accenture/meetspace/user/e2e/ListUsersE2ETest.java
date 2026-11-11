package com.accenture.meetspace.user.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
@DisplayName("GET /api/v1/users")
class ListUsersE2ETest {

    @Autowired
    RestTestClient client;

    @Autowired
    UserFixtures fixtures;

    private UUID akumar;

    @BeforeEach
    void givenUsers() {
        fixtures.adminId("root");
        fixtures.registered("jsmith");
        akumar = fixtures.registered("akumar");
    }

    private RestTestClient.ResponseSpec list(String query) {
        return client.get()
                .uri("/api/v1/users" + query)
                .header(HttpHeaders.AUTHORIZATION, fixtures.adminBearer("root"))
                .exchange();
    }

    private void assertUsernames(String query, String... usernames) {
        list(query)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[*].username").value(List.class, found ->
                        assertThat(found).containsExactly((Object[]) usernames));
    }

    @Nested
    @DisplayName("for an admin")
    class ForAnAdmin {

        @Test
        @DisplayName("lists every user sorted by username by default")
        void listsEveryUser() {
            assertUsernames("", "akumar", "jsmith", "root");
        }

        @Test
        @DisplayName("filters by username")
        void filtersByUsername() {
            assertUsernames("?username=smi", "jsmith");
        }

        @Test
        @DisplayName("filters by email")
        void filtersByEmail() {
            assertUsernames("?email=akumar@accenture.com", "akumar");
        }

        @Test
        @DisplayName("filters by department")
        void filtersByDepartment() {
            client.patch()
                    .uri("/api/v1/profile")
                    .header(HttpHeaders.AUTHORIZATION, fixtures.employeeBearer("jsmith"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("department", "Engineering"))
                    .exchange()
                    .expectStatus().isOk();

            assertUsernames("?department=Engineering", "jsmith");
        }

        @Test
        @DisplayName("filters by role")
        void filtersByRole() {
            assertUsernames("?role=ADMIN", "root");
        }

        @Test
        @DisplayName("filters by active status")
        void filtersByActiveStatus() {
            client.patch()
                    .uri("/api/v1/users/{id}/status", akumar)
                    .header(HttpHeaders.AUTHORIZATION, fixtures.adminBearer("root"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("active", false))
                    .exchange()
                    .expectStatus().isOk();

            assertUsernames("?active=false", "akumar");
        }

        @Test
        @DisplayName("sorts on request")
        void sortsOnRequest() {
            assertUsernames("?sort=username,desc", "root", "jsmith", "akumar");
        }

        @Test
        @DisplayName("returns the requested page with its metadata")
        void returnsTheRequestedPage() {
            list("?page=1&size=2")
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content[0].username").isEqualTo("root")
                    .jsonPath("$.page.size").isEqualTo(2)
                    .jsonPath("$.page.number").isEqualTo(1)
                    .jsonPath("$.page.totalElements").isEqualTo(3)
                    .jsonPath("$.page.totalPages").isEqualTo(2);
        }

        @Test
        @DisplayName("rejects an unknown sort with 400")
        void rejectsAnUnknownSort() {
            list("?sort=password")
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST");
        }
    }

    @Test
    @DisplayName("rejects an employee with 403")
    void rejectsAnEmployee() {
        client.get()
                .uri("/api/v1/users")
                .header(HttpHeaders.AUTHORIZATION, fixtures.employeeBearer("jsmith"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.code").isEqualTo("ACCESS_DENIED")
                .jsonPath("$.service").isEqualTo("user");
    }

    @Test
    @DisplayName("rejects a call without token with 401")
    void rejectsACallWithoutToken() {
        client.get()
                .uri("/api/v1/users")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("UNAUTHENTICATED");
    }
}
