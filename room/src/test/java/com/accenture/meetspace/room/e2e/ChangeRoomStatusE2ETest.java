package com.accenture.meetspace.room.e2e;

import com.accenture.meetspace.room.application.usecases.command.create.CreateRoomResponse;
import com.accenture.meetspace.room.application.usecases.command.create.CreateSiteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RoomE2ETest
@DisplayName("PATCH /api/v1/rooms/{id}/status")
class ChangeRoomStatusE2ETest {

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
        dodoId = room.roomId();
    }

    private RestTestClient.ResponseSpec changeStatus(Object id, Object body) {
        return client.patch()
                .uri("/api/v1/rooms/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange();
    }

    @Nested
    @DisplayName("changes the status")
    class ChangesTheStatus {

        @Test
        @DisplayName("returning 200, the room id and its new status")
        void returning200TheRoomIdAndItsNewStatus() {
            changeStatus(dodoId, Map.of("status", "MAINTENANCE"))
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.roomId").isEqualTo(dodoId.toString())
                    .jsonPath("$.status").isEqualTo("MAINTENANCE")
                    .jsonPath("$.changed").isEqualTo(true);
        }

        @Test
        @DisplayName("visible on the room and in the filtered list of the site")
        void visibleOnTheRoomAndInTheFilteredListOfTheSite() {
            changeStatus(dodoId, Map.of("status", "MAINTENANCE")).expectStatus().isOk();

            client.get()
                    .uri("/api/v1/rooms/{id}", dodoId)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("MAINTENANCE");
            client.get()
                    .uri("/api/v1/sites/" + ebeneId + "/rooms?status=MAINTENANCE")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content[*].code")
                    .value(List.class, codes -> assertThat(codes).containsExactly("R101"));
        }

        @Test
        @DisplayName("reactivating a room under maintenance")
        void reactivatingARoomUnderMaintenance() {
            changeStatus(dodoId, Map.of("status", "MAINTENANCE")).expectStatus().isOk();

            changeStatus(dodoId, Map.of("status", "ACTIVE"))
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ACTIVE");
        }
    }

    @Nested
    @DisplayName("rejects an invalid request")
    class RejectsAnInvalidRequest {

        @Test
        @DisplayName("without status")
        void withoutStatus() {
            changeStatus(dodoId, Map.of())
                    .expectStatus().isBadRequest()
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.service").isEqualTo("room")
                    .jsonPath("$.errors[0].field").isEqualTo("status");
        }

        @Test
        @DisplayName("with an unknown status")
        void withAnUnknownStatus() {
            changeStatus(dodoId, Map.of("status", "BROKEN"))
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("MALFORMED_REQUEST");
        }

        @Test
        @DisplayName("with an id that is not a UUID")
        void withAnIdThatIsNotAUuid() {
            changeStatus("not-a-uuid", Map.of("status", "CLOSED"))
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.errors[0].field").isEqualTo("id");
        }
    }

    @Test
    @DisplayName("refuses an unknown room")
    void refusesAnUnknownRoom() {
        changeStatus(UUID.randomUUID(), Map.of("status", "CLOSED"))
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.code").isEqualTo("ROOM_NOT_FOUND")
                .jsonPath("$.service").isEqualTo("room");
    }

    @Test
    @DisplayName("is documented in the OpenAPI description")
    void isDocumentedInTheOpenApiDescription() {
        client.get()
                .uri("/v3/api-docs")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.paths['/api/v1/rooms/{id}/status'].patch").exists();
    }
}
