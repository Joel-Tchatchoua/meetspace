package com.accenture.meetspace.room.e2e;

import com.accenture.meetspace.room.application.usecases.create.CreateSiteResponse;
import com.accenture.meetspace.shared.e2e.E2ETest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@E2ETest
@DisplayName("PUT /api/v1/sites/{id}")
class UpdateSiteE2ETest {

    private static final String SITES = "/api/v1/sites";

    @Autowired
    RestTestClient client;

    private static Map<String, Object> site(String code, String name) {
        var body = new HashMap<String, Object>();
        body.put("code", code);
        body.put("name", name);
        body.put("address", "Cybercity, Ebène");
        body.put("timeZone", "Indian/Mauritius");
        return body;
    }

    private static Map<String, Object> siteWith(String field, Object value) {
        var body = site("EBN", "Ebène");
        body.put(field, value);
        return body;
    }

    private UUID existingSite(String code, String name) {
        var response = client.post()
                .uri(SITES)
                .contentType(MediaType.APPLICATION_JSON)
                .body(site(code, name))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CreateSiteResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response).isNotNull();
        return response.siteId();
    }

    private RestTestClient.ResponseSpec update(Object id, Object body) {
        return client.put()
                .uri(SITES + "/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange();
    }

    @Nested
    @DisplayName("updates the site")
    class Updates {

        @Test
        @DisplayName("returning 200 and the updated site id")
        void returning200AndTheUpdatedSiteId() {
            var id = existingSite("EBN", "Ebène");

            update(id, site("EBX", "Ebène Tower"))
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.siteId").isEqualTo(id.toString())
                    .jsonPath("$.updated").isEqualTo(true);
        }

        @Test
        @DisplayName("keeping its own code and name")
        void keepingItsOwnCodeAndName() {
            var id = existingSite("EBN", "Ebène");

            update(id, siteWith("address", "Tower 1, Ebène"))
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.updated").isEqualTo(true);
        }

        @Test
        @DisplayName("freeing its previous code and name for another site")
        void freeingItsPreviousCodeAndNameForAnotherSite() {
            var id = existingSite("EBN", "Ebène");
            update(id, site("EBX", "Ebène Tower")).expectStatus().isOk();

            client.post()
                    .uri(SITES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(site("EBN", "Ebène"))
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.created").isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("rejects an invalid request")
    class RejectsAnInvalidRequest {

        @Test
        @DisplayName("listing every missing field")
        void listingEveryMissingField() {
            var id = existingSite("EBN", "Ebène");

            update(id, Map.of())
                    .expectStatus().isBadRequest()
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(400)
                    .jsonPath("$.title").isEqualTo("Invalid request")
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.service").isEqualTo("room")
                    .jsonPath("$.timestamp").isNotEmpty()
                    .jsonPath("$.errors[*].field")
                    .value(List.class, fields -> assertThat(fields)
                            .containsExactlyInAnyOrder("code", "name", "address"));
        }

        @Test
        @DisplayName("with an id that is not a UUID")
        void withAnIdThatIsNotAUuid() {
            update("not-a-uuid", site("EBN", "Ebène"))
                    .expectStatus().isBadRequest()
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.service").isEqualTo("room")
                    .jsonPath("$.errors[0].field").isEqualTo("id")
                    .jsonPath("$.errors[0].message").isNotEmpty();
        }

        @Test
        @DisplayName("with an unknown time zone")
        void withAnUnknownTimeZone() {
            var id = existingSite("EBN", "Ebène");

            update(id, siteWith("timeZone", "Mars/Olympus"))
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.errors[0].field").isEqualTo("timeZone");
        }
    }

    @Nested
    @DisplayName("refuses")
    class Refuses {

        @Test
        @DisplayName("an unknown site")
        void anUnknownSite() {
            var unknown = UUID.randomUUID();

            update(unknown, site("EBN", "Ebène"))
                    .expectStatus().isNotFound()
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(404)
                    .jsonPath("$.title").isEqualTo("Resource not found")
                    .jsonPath("$.code").isEqualTo("SITE_NOT_FOUND")
                    .jsonPath("$.detail").isEqualTo("Site " + unknown + " not found")
                    .jsonPath("$.service").isEqualTo("room");
        }

        @Test
        @DisplayName("a code used by another site")
        void aCodeUsedByAnotherSite() {
            var id = existingSite("EBN", "Ebène");
            existingSite("PLU", "Port-Louis");

            update(id, site("PLU", "Ebène"))
                    .expectStatus().isEqualTo(409)
                    .expectBody()
                    .jsonPath("$.title").isEqualTo("Business rule violated")
                    .jsonPath("$.code").isEqualTo("SITE_CODE_ALREADY_USED")
                    .jsonPath("$.detail").isEqualTo("A site with code PLU already exists");
        }

        @Test
        @DisplayName("a name used by another site")
        void aNameUsedByAnotherSite() {
            var id = existingSite("EBN", "Ebène");
            existingSite("PLU", "Port-Louis");

            update(id, site("EBN", "Port-Louis"))
                    .expectStatus().isEqualTo(409)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("SITE_NAME_ALREADY_USED")
                    .jsonPath("$.detail").isEqualTo("A site named Port-Louis already exists");
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
                .jsonPath("$.paths['/api/v1/sites/{id}'].put").exists();
    }
}
