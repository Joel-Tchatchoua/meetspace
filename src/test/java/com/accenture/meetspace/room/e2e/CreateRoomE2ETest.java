package com.accenture.meetspace.room.e2e;

import com.accenture.meetspace.room.application.usecases.command.create.CreateRoomResponse;
import com.accenture.meetspace.room.application.usecases.command.create.CreateSiteResponse;
import com.accenture.meetspace.shared.e2e.E2ETest;
import org.junit.jupiter.api.BeforeEach;
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
@DisplayName("POST /api/v1/sites/{siteId}/rooms")
class CreateRoomE2ETest {

    @Autowired
    RestTestClient client;

    private UUID ebeneId;

    @BeforeEach
    void givenASite() {
        ebeneId = site("EBN", "Ebène");
    }

    private UUID site(String code, String name) {
        var response = client.post()
                .uri("/api/v1/sites")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("code", code, "name", name, "address", "Mauritius"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CreateSiteResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response).isNotNull();
        return response.siteId();
    }

    private static Map<String, Object> dodo() {
        var body = new HashMap<String, Object>();
        body.put("code", "R101");
        body.put("name", "Dodo");
        body.put("floor", 1);
        body.put("capacity", 8);
        body.put("amenities", List.of("PROJECTOR", "WHITEBOARD"));
        return body;
    }

    private static Map<String, Object> dodoWith(String field, Object value) {
        var body = dodo();
        body.put(field, value);
        return body;
    }

    private RestTestClient.ResponseSpec create(Object siteId, Object body) {
        return client.post()
                .uri("/api/v1/sites/{siteId}/rooms", siteId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange();
    }

    @Nested
    @DisplayName("creates the room")
    class Creates {

        @Test
        @DisplayName("returning 201, its id and its location")
        void returning201ItsIdAndItsLocation() {
            var result = create(ebeneId, dodo())
                    .expectStatus().isCreated()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(CreateRoomResponse.class)
                    .returnResult();

            var response = result.getResponseBody();
            assertThat(response).isNotNull();
            assertThat(response.roomId()).isNotNull();
            assertThat(response.created()).isTrue();
            assertThat(result.getResponseHeaders().getLocation())
                    .hasToString(result.getUrl().resolve("/api/v1/rooms/" + response.roomId()).toString());
        }

        @Test
        @DisplayName("without amenities and on a floor below ground")
        void withoutAmenitiesAndOnAFloorBelowGround() {
            var body = dodoWith("floor", -1);
            body.remove("amenities");

            create(ebeneId, body)
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.created").isEqualTo(true);
        }

        @Test
        @DisplayName("reusing a code taken in another site")
        void reusingACodeTakenInAnotherSite() {
            create(site("PLU", "Port-Louis"), dodo()).expectStatus().isCreated();

            create(ebeneId, dodo())
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
            create(ebeneId, Map.of())
                    .expectStatus().isBadRequest()
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(400)
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.service").isEqualTo("room")
                    .jsonPath("$.errors[*].field")
                    .value(List.class, fields -> assertThat(fields)
                            .containsExactlyInAnyOrder("code", "name", "floor", "capacity"));
        }

        @Test
        @DisplayName("with a capacity above 200")
        void withACapacityAbove200() {
            create(ebeneId, dodoWith("capacity", 201))
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.errors[0].field").isEqualTo("capacity");
        }

        @Test
        @DisplayName("with an unknown amenity")
        void withAnUnknownAmenity() {
            create(ebeneId, dodoWith("amenities", List.of("JACUZZI")))
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("MALFORMED_REQUEST");
        }

        @Test
        @DisplayName("with a site id that is not a UUID")
        void withASiteIdThatIsNotAUuid() {
            create("not-a-uuid", dodo())
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.errors[0].field").isEqualTo("siteId");
        }
    }

    @Nested
    @DisplayName("refuses")
    class Refuses {

        @Test
        @DisplayName("an unknown site")
        void anUnknownSite() {
            create(UUID.randomUUID(), dodo())
                    .expectStatus().isNotFound()
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("SITE_NOT_FOUND")
                    .jsonPath("$.service").isEqualTo("room");
        }

        @Test
        @DisplayName("a code already used in the site")
        void aCodeAlreadyUsedInTheSite() {
            create(ebeneId, dodo()).expectStatus().isCreated();

            create(ebeneId, dodoWith("name", "Dodo bis"))
                    .expectStatus().isEqualTo(409)
                    .expectBody()
                    .jsonPath("$.title").isEqualTo("Business rule violated")
                    .jsonPath("$.code").isEqualTo("ROOM_CODE_ALREADY_USED");
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
                .jsonPath("$.paths['/api/v1/sites/{siteId}/rooms'].post").exists();
    }
}
