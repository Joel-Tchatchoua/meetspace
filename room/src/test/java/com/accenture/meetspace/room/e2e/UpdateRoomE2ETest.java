package com.accenture.meetspace.room.e2e;

import com.accenture.meetspace.room.application.usecases.command.create.CreateRoomResponse;
import com.accenture.meetspace.room.application.usecases.command.create.CreateSiteResponse;
import com.accenture.meetspace.bootstrap.test.e2e.E2ETest;
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
@DisplayName("PUT /api/v1/rooms/{id}")
class UpdateRoomE2ETest {

    private static final String ROOM = "/api/v1/rooms/{id}";

    @Autowired
    RestTestClient client;

    private UUID ebeneId;
    private UUID dodoId;

    @BeforeEach
    void givenARoom() {
        ebeneId = site("EBN", "Ebène");
        dodoId = room(ebeneId, "R101");
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

    private UUID room(UUID siteId, String code) {
        var response = client.post()
                .uri("/api/v1/sites/{siteId}/rooms", siteId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("code", code, "name", "Room " + code, "floor", 1, "capacity", 8))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CreateRoomResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response).isNotNull();
        return response.roomId();
    }

    private static Map<String, Object> details(String code) {
        var body = new HashMap<String, Object>();
        body.put("code", code);
        body.put("name", "Dodo XL");
        body.put("floor", 3);
        body.put("capacity", 20);
        body.put("amenities", List.of("VIDEO_CONFERENCE"));
        return body;
    }

    private RestTestClient.ResponseSpec update(Object id, Object body) {
        return client.put()
                .uri(ROOM, id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange();
    }

    @Nested
    @DisplayName("updates the room")
    class Updates {

        @Test
        @DisplayName("returning 200 and the updated room id")
        void returning200AndTheUpdatedRoomId() {
            update(dodoId, details("R103"))
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.roomId").isEqualTo(dodoId.toString())
                    .jsonPath("$.updated").isEqualTo(true);
        }

        @Test
        @DisplayName("replacing every detail of the room")
        void replacingEveryDetailOfTheRoom() {
            update(dodoId, details("R103")).expectStatus().isOk();

            client.get()
                    .uri(ROOM, dodoId)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("R103")
                    .jsonPath("$.name").isEqualTo("Dodo XL")
                    .jsonPath("$.floor").isEqualTo(3)
                    .jsonPath("$.capacity").isEqualTo(20)
                    .jsonPath("$.amenities")
                    .value(List.class, amenities -> assertThat(amenities).containsExactly("VIDEO_CONFERENCE"))
                    .jsonPath("$.status").isEqualTo("ACTIVE")
                    .jsonPath("$.site.id").isEqualTo(ebeneId.toString());
        }

        @Test
        @DisplayName("keeping its own code")
        void keepingItsOwnCode() {
            update(dodoId, details("R101"))
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.updated").isEqualTo(true);
        }

        @Test
        @DisplayName("taking a code used in another site")
        void takingACodeUsedInAnotherSite() {
            room(site("PLU", "Port-Louis"), "R201");

            update(dodoId, details("R201")).expectStatus().isOk();
        }
    }

    @Nested
    @DisplayName("rejects an invalid request")
    class RejectsAnInvalidRequest {

        @Test
        @DisplayName("listing every missing field")
        void listingEveryMissingField() {
            update(dodoId, Map.of())
                    .expectStatus().isBadRequest()
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.service").isEqualTo("room")
                    .jsonPath("$.errors[*].field")
                    .value(List.class, fields -> assertThat(fields)
                            .containsExactlyInAnyOrder("code", "name", "floor", "capacity"));
        }

        @Test
        @DisplayName("with a capacity below 1")
        void withACapacityBelow1() {
            var body = details("R101");
            body.put("capacity", 0);

            update(dodoId, body)
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.errors[0].field").isEqualTo("capacity");
        }

        @Test
        @DisplayName("with an id that is not a UUID")
        void withAnIdThatIsNotAUuid() {
            update("not-a-uuid", details("R101"))
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.errors[0].field").isEqualTo("id");
        }
    }

    @Nested
    @DisplayName("refuses")
    class Refuses {

        @Test
        @DisplayName("an unknown room")
        void anUnknownRoom() {
            update(UUID.randomUUID(), details("R101"))
                    .expectStatus().isNotFound()
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("ROOM_NOT_FOUND")
                    .jsonPath("$.service").isEqualTo("room");
        }

        @Test
        @DisplayName("a code used by another room of the site")
        void aCodeUsedByAnotherRoomOfTheSite() {
            room(ebeneId, "R102");

            update(dodoId, details("R102"))
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
                .jsonPath("$.paths['/api/v1/rooms/{id}'].put").exists();
    }
}
