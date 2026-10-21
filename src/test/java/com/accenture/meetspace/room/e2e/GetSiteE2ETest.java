package com.accenture.meetspace.room.e2e;

import com.accenture.meetspace.shared.e2e.E2ETest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@E2ETest
@DisplayName("GET /api/v1/sites/{id}")
class GetSiteE2ETest {

    private static final String SITES = "/api/v1/sites";

    @Autowired
    RestTestClient client;

    private URI createdSite() {
        var location = client.post()
                .uri(SITES)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "code", "EBN", "name", "Ebène", "address", "Cybercity, Ebène", "timeZone", "Europe/Paris"))
                .exchange()
                .expectStatus().isCreated()
                .returnResult()
                .getResponseHeaders()
                .getLocation();
        assertThat(location).isNotNull();
        return location;
    }

    @Nested
    @DisplayName("returns the site")
    class ReturnsTheSite {

        @Test
        @DisplayName("found at the location given on creation")
        void foundAtTheLocationGivenOnCreation() {
            var location = createdSite();
            var id = location.getPath().substring(location.getPath().lastIndexOf('/') + 1);

            client.get()
                    .uri(location)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(id)
                    .jsonPath("$.code").isEqualTo("EBN")
                    .jsonPath("$.name").isEqualTo("Ebène")
                    .jsonPath("$.address").isEqualTo("Cybercity, Ebène")
                    .jsonPath("$.timeZone").isEqualTo("Europe/Paris");
        }
    }

    @Nested
    @DisplayName("refuses")
    class Refuses {

        @Test
        @DisplayName("an unknown site")
        void anUnknownSite() {
            var unknown = UUID.randomUUID();

            client.get()
                    .uri(SITES + "/{id}", unknown)
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(404)
                    .jsonPath("$.code").isEqualTo("SITE_NOT_FOUND")
                    .jsonPath("$.detail").isEqualTo("Site " + unknown + " not found")
                    .jsonPath("$.service").isEqualTo("room");
        }

        @Test
        @DisplayName("a deleted site")
        void aDeletedSite() {
            var location = createdSite();
            client.delete().uri(location).exchange().expectStatus().isOk();

            client.get()
                    .uri(location)
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("SITE_NOT_FOUND");
        }

        @Test
        @DisplayName("an id that is not a UUID")
        void anIdThatIsNotAUuid() {
            client.get()
                    .uri(SITES + "/not-a-uuid")
                    .exchange()
                    .expectStatus().isBadRequest()
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
                .jsonPath("$.paths['/api/v1/sites/{id}'].get").exists();
    }
}
