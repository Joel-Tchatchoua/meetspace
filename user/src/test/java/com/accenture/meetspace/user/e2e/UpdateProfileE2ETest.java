package com.accenture.meetspace.user.e2e;

import com.accenture.meetspace.user.units.stubs.InMemorySiteCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;
import java.util.UUID;

@UserE2ETest
@DisplayName("PATCH /api/v1/profile")
class UpdateProfileE2ETest {

    @Autowired
    RestTestClient client;

    @Autowired
    UserFixtures fixtures;

    @Autowired
    InMemorySiteCatalog sites;

    private RestTestClient.ResponseSpec update(Map<String, Object> body) {
        return client.patch()
                .uri("/api/v1/profile")
                .header(HttpHeaders.AUTHORIZATION, fixtures.employeeBearer("jsmith"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange();
    }

    @Test
    @DisplayName("changes only the fields that are sent")
    void changesOnlyTheFieldsThatAreSent() {
        var userId = fixtures.registered("jsmith");
        var siteId = UUID.randomUUID();
        sites.add(siteId);
        update(Map.of("department", "Engineering", "phone", "+230 5555 0000", "defaultSiteId", siteId))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userId").isEqualTo(userId.toString());

        update(Map.of("department", "Sales")).expectStatus().isOk();

        client.get()
                .uri("/api/v1/profile")
                .header(HttpHeaders.AUTHORIZATION, fixtures.employeeBearer("jsmith"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("John")
                .jsonPath("$.department").isEqualTo("Sales")
                .jsonPath("$.phone").isEqualTo("+230 5555 0000")
                .jsonPath("$.defaultSiteId").isEqualTo(siteId.toString());
    }

    @Test
    @DisplayName("rejects an unknown default site with 404")
    void rejectsAnUnknownDefaultSite() {
        fixtures.registered("jsmith");

        update(Map.of("defaultSiteId", UUID.randomUUID()))
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("DEFAULT_SITE_NOT_FOUND");
    }

    @Test
    @DisplayName("rejects a blank first name with 400")
    void rejectsABlankFirstName() {
        fixtures.registered("jsmith");

        update(Map.of("firstName", "  "))
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0].field").isEqualTo("firstName");
    }

    @Test
    @DisplayName("rejects a call without token with 401")
    void rejectsACallWithoutToken() {
        client.patch()
                .uri("/api/v1/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("department", "Sales"))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("UNAUTHENTICATED");
    }
}
