package com.accenture.meetspace.room.e2e;

import com.accenture.meetspace.room.application.usecases.command.create.CreateSiteResponse;
import com.accenture.meetspace.bootstrap.test.e2e.E2ETest;
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

@E2ETest
@DisplayName("GET /api/v1/sites/{siteId}/rooms")
class ListRoomsE2ETest {

    @Autowired
    RestTestClient client;

    private UUID ebeneId;

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

    private void room(UUID siteId, String code, String name, int floor, int capacity, List<String> amenities) {
        client.post()
                .uri("/api/v1/sites/{siteId}/rooms", siteId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("code", code, "name", name, "floor", floor, "capacity", capacity,
                        "amenities", amenities))
                .exchange()
                .expectStatus().isCreated();
    }

    private RestTestClient.ResponseSpec list(Object siteId, String query) {
        return client.get()
                .uri("/api/v1/sites/" + siteId + "/rooms" + query)
                .exchange();
    }

    private void expectCodes(String query, String... codes) {
        list(ebeneId, query)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[*].code")
                .value(List.class, found -> assertThat(found).containsExactly((Object[]) codes));
    }

    @Nested
    @DisplayName("lists the rooms of the site")
    class ListsTheRoomsOfTheSite {

        @BeforeEach
        void givenRooms() {
            ebeneId = site("EBN", "Ebène");
            room(ebeneId, "R201", "Pink Pigeon", 2, 20, List.of("PROJECTOR", "VIDEO_CONFERENCE"));
            room(ebeneId, "R101", "Dodo", 1, 8, List.of("WHITEBOARD", "PROJECTOR"));
            room(ebeneId, "R102", "Kestrel", 1, 4, List.of("WHITEBOARD"));
            room(site("PLU", "Port-Louis"), "R100", "Elsewhere", 1, 50, List.of("PROJECTOR"));
        }

        @Test
        @DisplayName("sorted by code by default, in the paginated format")
        void sortedByCodeByDefaultInThePaginatedFormat() {
            list(ebeneId, "")
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.content[*].code")
                    .value(List.class, codes -> assertThat(codes).containsExactly("R101", "R102", "R201"))
                    .jsonPath("$.content[0].name").isEqualTo("Dodo")
                    .jsonPath("$.content[0].floor").isEqualTo(1)
                    .jsonPath("$.content[0].capacity").isEqualTo(8)
                    .jsonPath("$.content[0].status").isEqualTo("ACTIVE")
                    .jsonPath("$.content[0].amenities")
                    .value(List.class, amenities -> assertThat(amenities).containsExactly("PROJECTOR", "WHITEBOARD"))
                    .jsonPath("$.content[0].site.id").isEqualTo(ebeneId.toString())
                    .jsonPath("$.page.size").isEqualTo(20)
                    .jsonPath("$.page.number").isEqualTo(0)
                    .jsonPath("$.page.totalElements").isEqualTo(3)
                    .jsonPath("$.page.totalPages").isEqualTo(1);
        }

        @Test
        @DisplayName("with a minimum capacity")
        void withAMinimumCapacity() {
            expectCodes("?minCapacity=8", "R101", "R201");
        }

        @Test
        @DisplayName("having every requested amenity")
        void havingEveryRequestedAmenity() {
            expectCodes("?amenities=PROJECTOR&amenities=WHITEBOARD", "R101");
            expectCodes("?amenities=PROJECTOR", "R101", "R201");
        }

        @Test
        @DisplayName("on a given floor")
        void onAGivenFloor() {
            expectCodes("?floor=2", "R201");
        }

        @Test
        @DisplayName("with a given status")
        void withAGivenStatus() {
            expectCodes("?status=ACTIVE", "R101", "R102", "R201");
            expectCodes("?status=CLOSED");
        }

        @Test
        @DisplayName("combining filters")
        void combiningFilters() {
            expectCodes("?floor=1&amenities=WHITEBOARD&minCapacity=5", "R101");
        }

        @Test
        @DisplayName("sorted by the requested property and direction")
        void sortedByTheRequestedPropertyAndDirection() {
            expectCodes("?sort=capacity,desc", "R201", "R101", "R102");
        }

        @Test
        @DisplayName("one page at a time")
        void onePageAtATime() {
            list(ebeneId, "?page=1&size=2")
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content[*].code").value(List.class, codes -> assertThat(codes).containsExactly("R201"))
                    .jsonPath("$.page.totalElements").isEqualTo(3)
                    .jsonPath("$.page.totalPages").isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("refuses")
    class Refuses {

        @Test
        @DisplayName("an unknown site")
        void anUnknownSite() {
            list(UUID.randomUUID(), "")
                    .expectStatus().isNotFound()
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("SITE_NOT_FOUND")
                    .jsonPath("$.service").isEqualTo("room");
        }

        @Test
        @DisplayName("an unknown status")
        void anUnknownStatus() {
            list(site("EBN", "Ebène"), "?status=BROKEN")
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.errors[0].field").isEqualTo("status");
        }

        @Test
        @DisplayName("an unknown amenity")
        void anUnknownAmenity() {
            list(site("EBN", "Ebène"), "?amenities=JACUZZI")
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.errors[0].field").isEqualTo("amenities");
        }

        @Test
        @DisplayName("a minimum capacity below 1")
        void aMinimumCapacityBelow1() {
            list(site("EBN", "Ebène"), "?minCapacity=0")
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.errors[0].field").isEqualTo("minCapacity");
        }

        @Test
        @DisplayName("an unknown sort property")
        void anUnknownSortProperty() {
            list(site("EBN", "Ebène"), "?sort=password")
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.errors[0].field").isEqualTo("sort");
        }
    }

    @Test
    @DisplayName("is documented in the OpenAPI description with its filters")
    void isDocumentedInTheOpenApiDescriptionWithItsFilters() {
        client.get()
                .uri("/v3/api-docs")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.paths['/api/v1/sites/{siteId}/rooms'].get.parameters[*].name")
                .value(List.class, names -> assertThat(names)
                        .contains("minCapacity", "amenities", "floor", "status", "page", "size", "sort"));
    }
}
