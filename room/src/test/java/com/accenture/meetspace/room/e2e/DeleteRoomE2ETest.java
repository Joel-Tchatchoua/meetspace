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

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@E2ETest
@DisplayName("DELETE /api/v1/rooms/{id}")
class DeleteRoomE2ETest {

    private static final String ROOM = "/api/v1/rooms/{id}";

    @Autowired
    RestTestClient client;

    private UUID ebeneId;
    private UUID dodoId;

    @BeforeEach
    void givenARoom() {
        var site = client.post()
                .uri("/api/v1/sites")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("code", "EBN", "name", "Ebène", "address", "Mauritius"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CreateSiteResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(site).isNotNull();
        ebeneId = site.siteId();
        dodoId = createDodo();
    }

    private UUID createDodo() {
        var room = client.post()
                .uri("/api/v1/sites/{siteId}/rooms", ebeneId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("code", "R101", "name", "Dodo", "floor", 1, "capacity", 8))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CreateRoomResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(room).isNotNull();
        return room.roomId();
    }

    private RestTestClient.ResponseSpec delete(Object id) {
        return client.delete()
                .uri(ROOM, id)
                .exchange();
    }

    @Nested
    @DisplayName("removes the room")
    class RemovesTheRoom {

        @Test
        @DisplayName("returning 200 and the removed room id")
        void returning200AndTheRemovedRoomId() {
            delete(dodoId)
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.roomId").isEqualTo(dodoId.toString())
                    .jsonPath("$.deleted").isEqualTo(true);
        }

        @Test
        @DisplayName("making it unknown and absent from the site rooms")
        void makingItUnknownAndAbsentFromTheSiteRooms() {
            delete(dodoId).expectStatus().isOk();

            client.get()
                    .uri(ROOM, dodoId)
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("ROOM_NOT_FOUND");
            client.get()
                    .uri("/api/v1/sites/{siteId}/rooms", ebeneId)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.page.totalElements").isEqualTo(0);
        }

        @Test
        @DisplayName("freeing its code in the site")
        void freeingItsCodeInTheSite() {
            delete(dodoId).expectStatus().isOk();

            assertThat(createDodo()).isNotEqualTo(dodoId);
        }

        @Test
        @DisplayName("letting its site be deleted once it has no room left")
        void lettingItsSiteBeDeletedOnceItHasNoRoomLeft() {
            delete(dodoId).expectStatus().isOk();

            client.delete()
                    .uri("/api/v1/sites/{id}", ebeneId)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.deleted").isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("refuses")
    class Refuses {

        @Test
        @DisplayName("a room already removed")
        void aRoomAlreadyRemoved() {
            delete(dodoId).expectStatus().isOk();

            delete(dodoId)
                    .expectStatus().isNotFound()
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("ROOM_NOT_FOUND")
                    .jsonPath("$.service").isEqualTo("room");
        }

        @Test
        @DisplayName("an unknown room")
        void anUnknownRoom() {
            var unknown = UUID.randomUUID();

            delete(unknown)
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("ROOM_NOT_FOUND")
                    .jsonPath("$.detail").isEqualTo("Room " + unknown + " not found");
        }

        @Test
        @DisplayName("an id that is not a UUID")
        void anIdThatIsNotAUuid() {
            delete("not-a-uuid")
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
                .jsonPath("$.paths['/api/v1/rooms/{id}'].delete").exists();
    }
}
