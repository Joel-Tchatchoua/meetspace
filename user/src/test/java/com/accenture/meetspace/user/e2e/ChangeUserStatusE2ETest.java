package com.accenture.meetspace.user.e2e;

import com.accenture.meetspace.user.units.stubs.InMemoryReservationCanceller;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UserE2ETest
@DisplayName("PATCH /api/v1/users/{id}/status")
class ChangeUserStatusE2ETest {

    @Autowired
    RestTestClient client;

    @Autowired
    UserFixtures fixtures;

    @Autowired
    InMemoryReservationCanceller reservations;

    private RestTestClient.ResponseSpec changeStatus(Object id, boolean active, String bearer) {
        return client.patch()
                .uri("/api/v1/users/{id}/status", id)
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("active", active))
                .exchange();
    }

    @Nested
    @DisplayName("deactivates a user")
    class Deactivates {

        @Test
        @DisplayName("and answers with the new status")
        void andAnswersWithTheNewStatus() {
            var userId = fixtures.registered("jsmith");

            changeStatus(userId, false, fixtures.adminBearer("root"))
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.userId").isEqualTo(userId.toString())
                    .jsonPath("$.active").isEqualTo(false);
        }

        @Test
        @DisplayName("and cancels their reservations")
        void andCancelsTheirReservations() {
            var userId = fixtures.registered("jsmith");

            changeStatus(userId, false, fixtures.adminBearer("root")).expectStatus().isOk();

            assertThat(reservations.cancelledFor()).contains(userId);
        }

        @Test
        @DisplayName("who then gets 403 on any endpoint, even with a valid token")
        void whoThenGets403OnAnyEndpoint() {
            var userId = fixtures.registered("jsmith");
            changeStatus(userId, false, fixtures.adminBearer("root")).expectStatus().isOk();

            client.get()
                    .uri("/api/v1/profile")
                    .header(HttpHeaders.AUTHORIZATION, fixtures.employeeBearer("jsmith"))
                    .exchange()
                    .expectStatus().isForbidden()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("ACCOUNT_DISABLED")
                    .jsonPath("$.service").isEqualTo("meetspace");
        }
    }

    @Test
    @DisplayName("activates a user again")
    void activatesAUserAgain() {
        var userId = fixtures.registered("jsmith");
        changeStatus(userId, false, fixtures.adminBearer("root")).expectStatus().isOk();

        changeStatus(userId, true, fixtures.adminBearer("root"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.active").isEqualTo(true);

        client.get()
                .uri("/api/v1/profile")
                .header(HttpHeaders.AUTHORIZATION, fixtures.employeeBearer("jsmith"))
                .exchange()
                .expectStatus().isOk();
    }

    @Nested
    @DisplayName("rejects")
    class Rejects {

        @Test
        @DisplayName("an admin deactivating their own account with 409")
        void anAdminDeactivatingTheirOwnAccount() {
            var adminId = fixtures.adminId("root");

            changeStatus(adminId, false, fixtures.adminBearer("root"))
                    .expectStatus().isEqualTo(409)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("CANNOT_DEACTIVATE_OWN_ACCOUNT");
        }

        @Test
        @DisplayName("an unknown user with 404")
        void anUnknownUser() {
            changeStatus(UUID.randomUUID(), false, fixtures.adminBearer("root"))
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("USER_NOT_FOUND");
        }

        @Test
        @DisplayName("an employee with 403")
        void anEmployee() {
            var userId = fixtures.registered("jsmith");

            changeStatus(userId, false, fixtures.employeeBearer("jsmith"))
                    .expectStatus().isForbidden()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("ACCESS_DENIED");
        }

        @Test
        @DisplayName("a missing status with 400")
        void aMissingStatus() {
            var userId = fixtures.registered("jsmith");

            client.patch()
                    .uri("/api/v1/users/{id}/status", userId)
                    .header(HttpHeaders.AUTHORIZATION, fixtures.adminBearer("root"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of())
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.errors[0].field").isEqualTo("active");
        }
    }
}
