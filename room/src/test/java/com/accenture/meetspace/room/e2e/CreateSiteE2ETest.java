package com.accenture.meetspace.room.e2e;

import com.accenture.meetspace.room.application.usecases.command.create.CreateSiteResponse;
import com.accenture.meetspace.bootstrap.test.e2e.E2ETest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@E2ETest
@DisplayName("POST /api/v1/sites")
class CreateSiteE2ETest {

    private static final String SITES = "/api/v1/sites";

    @Autowired
    RestTestClient client;

    private static Map<String, Object> ebene() {
        var body = new HashMap<String, Object>();
        body.put("code", "EBN");
        body.put("name", "Ebène");
        body.put("address", "Cybercity, Ebène");
        body.put("timeZone", "Europe/Paris");
        return body;
    }

    private static Map<String, Object> ebeneWith(String field, Object value) {
        var body = ebene();
        body.put(field, value);
        return body;
    }

    private RestTestClient.ResponseSpec create(Object body) {
        return client.post()
                .uri(SITES)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange();
    }

    @Nested
    @DisplayName("creates the site")
    class Creates {

        @Test
        @DisplayName("returning 201, its id and its location")
        void returning201ItsIdAndItsLocation() {
            var result = create(ebene())
                    .expectStatus().isCreated()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(CreateSiteResponse.class)
                    .returnResult();

            var response = result.getResponseBody();
            assertThat(response).isNotNull();
            assertThat(response.siteId()).isNotNull();
            assertThat(response.created()).isTrue();
            assertThat(result.getResponseHeaders().getLocation())
                    .hasToString(result.getUrl() + "/" + response.siteId());
        }

        @Test
        @DisplayName("accepting a site without time zone")
        void acceptingASiteWithoutTimeZone() {
            var body = ebene();
            body.remove("timeZone");

            create(body)
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.siteId").isNotEmpty()
                    .jsonPath("$.created").isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("rejects an invalid request")
    class RejectsAnInvalidRequest {

        @Test
        @DisplayName("listing every missing field")
        void listingEveryMissingField() {
            create(Map.of())
                    .expectStatus().isBadRequest()
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(400)
                    .jsonPath("$.title").isEqualTo("Invalid request")
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.service").isEqualTo("room")
                    .jsonPath("$.timestamp").isNotEmpty()
                    .jsonPath("$.instance").isEqualTo(SITES)
                    .jsonPath("$.errors[*].field")
                    .value(List.class, fields -> assertThat(fields)
                            .containsExactlyInAnyOrder("code", "name", "address"));
        }

        @Test
        @DisplayName("with a code longer than 50 characters")
        void withACodeLongerThan50Characters() {
            create(ebeneWith("code", "X".repeat(51)))
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.errors[0].field").isEqualTo("code")
                    .jsonPath("$.errors[0].message").isNotEmpty();
        }

        @Test
        @DisplayName("with an unknown time zone")
        void withAnUnknownTimeZone() {
            create(ebeneWith("timeZone", "Mars/Olympus"))
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.errors[0].field").isEqualTo("timeZone")
                    .jsonPath("$.errors[0].message")
                    .value(String.class, message -> assertThat(message).endsWith("Mars/Olympus"));
        }

        @Test
        @DisplayName("with a malformed body")
        void withAMalformedBody() {
            create("{ not json")
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(400)
                    .jsonPath("$.code").isEqualTo("MALFORMED_REQUEST")
                    .jsonPath("$.service").isEqualTo("room");
        }
    }

    @Nested
    @DisplayName("checks the API version")
    class ChecksTheApiVersion {

        @Test
        @DisplayName("rejecting an unsupported version")
        void rejectingAnUnsupportedVersion() {
            client.post()
                    .uri("/api/v2/sites")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ebene())
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(400)
                    .jsonPath("$.detail").value(String.class, detail -> assertThat(detail).contains("2"));
        }

        @Test
        @DisplayName("documenting the versioned path in the OpenAPI description")
        void documentingTheVersionedPathInTheOpenApiDescription() {
            client.get()
                    .uri("/v3/api-docs")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.paths['/api/v1/sites'].post").exists();
        }
    }

    @Nested
    @DisplayName("refuses a conflicting site")
    class RefusesAConflictingSite {

        @Test
        @DisplayName("when the code is already used")
        void whenTheCodeIsAlreadyUsed() {
            create(ebene()).expectStatus().isCreated();

            create(ebeneWith("name", "Ebène Tower"))
                    .expectStatus().isEqualTo(409)
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(409)
                    .jsonPath("$.title").isEqualTo("Business rule violated")
                    .jsonPath("$.code").isEqualTo("SITE_CODE_ALREADY_USED")
                    .jsonPath("$.detail").isEqualTo("A site with code EBN already exists")
                    .jsonPath("$.service").isEqualTo("room")
                    .jsonPath("$.timestamp").isNotEmpty();
        }

        @Test
        @DisplayName("when the name is already used")
        void whenTheNameIsAlreadyUsed() {
            create(ebene()).expectStatus().isCreated();

            create(ebeneWith("code", "EBX"))
                    .expectStatus().isEqualTo(409)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("SITE_NAME_ALREADY_USED")
                    .jsonPath("$.detail").isEqualTo("A site named Ebène already exists");
        }
    }
}
