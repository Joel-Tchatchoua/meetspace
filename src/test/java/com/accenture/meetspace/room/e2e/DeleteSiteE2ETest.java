package com.accenture.meetspace.room.e2e;

import com.accenture.meetspace.room.application.usecases.command.create.CreateSiteResponse;
import com.accenture.meetspace.shared.e2e.E2ETest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@E2ETest
@DisplayName("DELETE /api/v1/sites/{id}")
class DeleteSiteE2ETest {

    private static final String SITES = "/api/v1/sites";

    @Autowired
    RestTestClient client;

    private static Map<String, Object> ebene() {
        return Map.of("code", "EBN", "name", "Ebène", "address", "Cybercity, Ebène");
    }

    private RestTestClient.ResponseSpec create(Map<String, Object> body) {
        return client.post()
                .uri(SITES)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange();
    }

    private UUID existingSite() {
        var response = create(ebene())
                .expectStatus().isCreated()
                .expectBody(CreateSiteResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response).isNotNull();
        return response.siteId();
    }

    private RestTestClient.ResponseSpec delete(Object id) {
        return client.delete()
                .uri(SITES + "/{id}", id)
                .exchange();
    }

    @Nested
    @DisplayName("deletes the site")
    class Deletes {

        @Test
        @DisplayName("returning 200 and the deleted site id")
        void returning200AndTheDeletedSiteId() {
            var id = existingSite();

            delete(id)
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.siteId").isEqualTo(id.toString())
                    .jsonPath("$.deleted").isEqualTo(true);
        }

        @Test
        @DisplayName("making it unknown afterwards")
        void makingItUnknownAfterwards() {
            var id = existingSite();
            delete(id).expectStatus().isOk();

            delete(id)
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("SITE_NOT_FOUND");
        }

        @Test
        @DisplayName("freeing its code and name for a new site")
        void freeingItsCodeAndNameForANewSite() {
            delete(existingSite()).expectStatus().isOk();

            create(ebene())
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.created").isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("refuses")
    class Refuses {

        @Test
        @DisplayName("a site that still has rooms")
        void aSiteThatStillHasRooms() {
            var id = existingSite();
            client.post()
                    .uri(SITES + "/{id}/rooms", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("code", "R101", "name", "Dodo", "floor", 1, "capacity", 8))
                    .exchange()
                    .expectStatus().isCreated();

            delete(id)
                    .expectStatus().isEqualTo(409)
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.title").isEqualTo("Business rule violated")
                    .jsonPath("$.code").isEqualTo("SITE_HAS_ROOMS")
                    .jsonPath("$.service").isEqualTo("room");
        }

        @Test
        @DisplayName("an unknown site")
        void anUnknownSite() {
            var unknown = UUID.randomUUID();

            delete(unknown)
                    .expectStatus().isNotFound()
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(404)
                    .jsonPath("$.title").isEqualTo("Resource not found")
                    .jsonPath("$.code").isEqualTo("SITE_NOT_FOUND")
                    .jsonPath("$.detail").isEqualTo("Site " + unknown + " not found")
                    .jsonPath("$.service").isEqualTo("room")
                    .jsonPath("$.timestamp").isNotEmpty();
        }

        @Test
        @DisplayName("an id that is not a UUID")
        void anIdThatIsNotAUuid() {
            delete("not-a-uuid")
                    .expectStatus().isBadRequest()
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.errors[0].field").isEqualTo("id");
        }
    }

    @Test
    @DisplayName("is documented in the OpenAPI description")
    void isDocumentedInTheOpenApiDescription() {
        client.get()
                .uri("/v3/api-docs")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.paths['/api/v1/sites/{id}'].delete").exists();
    }
}
