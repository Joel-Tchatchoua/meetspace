package com.accenture.meetspace.room.e2e;

import com.accenture.meetspace.room.application.usecases.create.CreateSiteResponse;
import com.accenture.meetspace.shared.e2e.E2ETest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;

@E2ETest
@DisplayName("POST /api/v1/sites")
class CreateSiteE2ETest {

    private static final String SITES = "/api/v1/sites";

    @Autowired
    WebTestClient client;

    @Autowired
    JdbcTemplate jdbc;

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

    private WebTestClient.ResponseSpec create(Object body) {
        return client.post()
                .uri(SITES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
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
                    .expectBody(CreateSiteResponse.class)
                    .returnResult();

            var response = result.getResponseBody();
            assertThat(response).isNotNull();
            assertThat(response.created()).isTrue();
            assertThat(result.getResponseHeaders().getLocation())
                    .hasToString(result.getUrl() + "/" + response.siteId());
            assertThat(jdbc.queryForMap("select code, name, address, time_zone from sites where id = ?",
                    response.siteId().toString()))
                    .containsEntry("code", "EBN")
                    .containsEntry("name", "Ebène")
                    .containsEntry("address", "Cybercity, Ebène")
                    .containsEntry("time_zone", "Europe/Paris");
        }

        @Test
        @DisplayName("with Indian/Mauritius as default time zone")
        void withIndianMauritiusAsDefaultTimeZone() {
            var body = ebene();
            body.remove("timeZone");

            var response = create(body)
                    .expectStatus().isCreated()
                    .expectBody(CreateSiteResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            assertThat(jdbc.queryForObject("select time_zone from sites where id = ?", String.class,
                    response.siteId().toString()))
                    .isEqualTo("Indian/Mauritius");
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
                    .jsonPath("$.errors[*].field").value(containsInAnyOrder("code", "name", "address"));
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
                    .jsonPath("$.errors[0].message").value(endsWith("Mars/Olympus"));
        }

        @Test
        @DisplayName("with a malformed body")
        void withAMalformedBody() {
            client.post()
                    .uri(SITES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{ not json")
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(400)
                    .jsonPath("$.code").isEqualTo("MALFORMED_REQUEST")
                    .jsonPath("$.service").isEqualTo("room");
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
