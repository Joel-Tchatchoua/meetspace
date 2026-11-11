package com.accenture.meetspace.room.e2e;

import com.accenture.meetspace.room.application.usecases.command.create.CreateSiteResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RoomE2ETest
@DisplayName("GET /api/v1/rooms/{id}")
class GetRoomE2ETest {

    @Autowired
    RestTestClient client;

    private UUID site() {
        var response = client.post()
                .uri("/api/v1/sites")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("code", "EBN", "name", "Ebène", "address", "Mauritius", "timeZone", "Europe/Paris"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CreateSiteResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response).isNotNull();
        return response.siteId();
    }

    private URI room(UUID siteId) {
        return client.post()
                .uri("/api/v1/sites/{siteId}/rooms", siteId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("code", "R101", "name", "Dodo", "floor", 1, "capacity", 8,
                        "amenities", List.of("WHITEBOARD", "PROJECTOR")))
                .exchange()
                .expectStatus().isCreated()
                .returnResult()
                .getResponseHeaders()
                .getLocation();
    }

    @Nested
    @DisplayName("returns the room")
    class ReturnsTheRoom {

        @Test
        @DisplayName("with its site and the site time zone, at the location given on creation")
        void withItsSiteAndTheSiteTimeZoneAtTheLocationGivenOnCreation() {
            var siteId = site();
            var location = room(siteId);
            var roomId = location.getPath().substring(location.getPath().lastIndexOf('/') + 1);

            client.get()
                    .uri(location)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(roomId)
                    .jsonPath("$.code").isEqualTo("R101")
                    .jsonPath("$.name").isEqualTo("Dodo")
                    .jsonPath("$.floor").isEqualTo(1)
                    .jsonPath("$.capacity").isEqualTo(8)
                    .jsonPath("$.status").isEqualTo("ACTIVE")
                    .jsonPath("$.amenities")
                    .value(List.class, amenities -> assertThat(amenities).containsExactly("PROJECTOR", "WHITEBOARD"))
                    .jsonPath("$.site.id").isEqualTo(siteId.toString())
                    .jsonPath("$.site.code").isEqualTo("EBN")
                    .jsonPath("$.site.name").isEqualTo("Ebène")
                    .jsonPath("$.site.timeZone").isEqualTo("Europe/Paris");
        }
    }

    @Nested
    @DisplayName("refuses")
    class Refuses {

        @Test
        @DisplayName("an unknown room")
        void anUnknownRoom() {
            var unknown = UUID.randomUUID();

            client.get()
                    .uri("/api/v1/rooms/{id}", unknown)
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(404)
                    .jsonPath("$.code").isEqualTo("ROOM_NOT_FOUND")
                    .jsonPath("$.detail").isEqualTo("Room " + unknown + " not found")
                    .jsonPath("$.service").isEqualTo("room");
        }

        @Test
        @DisplayName("an id that is not a UUID")
        void anIdThatIsNotAUuid() {
            client.get()
                    .uri("/api/v1/rooms/not-a-uuid")
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
                .jsonPath("$.paths['/api/v1/rooms/{id}'].get").exists();
    }
}
