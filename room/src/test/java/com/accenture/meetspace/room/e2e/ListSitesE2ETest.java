package com.accenture.meetspace.room.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RoomE2ETest
@DisplayName("GET /api/v1/sites")
class ListSitesE2ETest {

    private static final String SITES = "/api/v1/sites";

    @Autowired
    RestTestClient client;

    private URI create(String code, String name) {
        return client.post()
                .uri(SITES)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("code", code, "name", name, "address", "Mauritius"))
                .exchange()
                .expectStatus().isCreated()
                .returnResult()
                .getResponseHeaders()
                .getLocation();
    }

    private RestTestClient.ResponseSpec list(String query) {
        return client.get()
                .uri(SITES + query)
                .exchange();
    }

    @Nested
    @DisplayName("lists the sites")
    class ListsTheSites {

        @BeforeEach
        void givenThreeSites() {
            create("PLU", "Port-Louis");
            create("EBN", "Ebène");
            create("CUR", "Curepipe");
        }

        @Test
        @DisplayName("sorted by code by default, in the paginated format")
        void sortedByCodeByDefaultInThePaginatedFormat() {
            list("")
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.content[*].code")
                    .value(List.class, codes -> assertThat(codes).containsExactly("CUR", "EBN", "PLU"))
                    .jsonPath("$.content[0].name").isEqualTo("Curepipe")
                    .jsonPath("$.content[0].timeZone").isEqualTo("Indian/Mauritius")
                    .jsonPath("$.page.size").isEqualTo(20)
                    .jsonPath("$.page.number").isEqualTo(0)
                    .jsonPath("$.page.totalElements").isEqualTo(3)
                    .jsonPath("$.page.totalPages").isEqualTo(1);
        }

        @Test
        @DisplayName("one page at a time")
        void onePageAtATime() {
            list("?page=1&size=2")
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content[*].code").value(List.class, codes -> assertThat(codes).containsExactly("PLU"))
                    .jsonPath("$.page.size").isEqualTo(2)
                    .jsonPath("$.page.number").isEqualTo(1)
                    .jsonPath("$.page.totalElements").isEqualTo(3)
                    .jsonPath("$.page.totalPages").isEqualTo(2);
        }

        @Test
        @DisplayName("sorted by the requested property and direction")
        void sortedByTheRequestedPropertyAndDirection() {
            list("?sort=name,desc")
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content[*].name")
                    .value(List.class, names -> assertThat(names).containsExactly("Port-Louis", "Ebène", "Curepipe"));
        }

        @Test
        @DisplayName("without the deleted ones")
        void withoutTheDeletedOnes() {
            client.delete().uri(create("OLD", "Old site")).exchange().expectStatus().isOk();

            list("")
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content[*].code")
                    .value(List.class, codes -> assertThat(codes).containsExactly("CUR", "EBN", "PLU"))
                    .jsonPath("$.page.totalElements").isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("rejects")
    class Rejects {

        @Test
        @DisplayName("a negative page")
        void aNegativePage() {
            list("?page=-1")
                    .expectStatus().isBadRequest()
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.service").isEqualTo("room")
                    .jsonPath("$.errors[0].field").isEqualTo("page");
        }

        @Test
        @DisplayName("a page size that is not a number")
        void aPageSizeThatIsNotANumber() {
            list("?size=ten")
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.errors[0].field").isEqualTo("size")
                    .jsonPath("$.errors[0].message").value(String.class, message -> assertThat(message)
                            .contains("ten"));
        }

        @Test
        @DisplayName("an unknown sort direction")
        void anUnknownSortDirection() {
            list("?sort=name,sideways")
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.errors[0].field").isEqualTo("sort")
                    .jsonPath("$.errors[0].message").value(String.class, message -> assertThat(message)
                            .contains("sideways"));
        }

        @Test
        @DisplayName("an unknown sort property")
        void anUnknownSortProperty() {
            list("?sort=password")
                    .expectStatus().isBadRequest()
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.service").isEqualTo("room")
                    .jsonPath("$.errors[0].field").isEqualTo("sort")
                    .jsonPath("$.errors[0].message").value(String.class, message -> assertThat(message)
                            .contains("password"));
        }

        @Test
        @DisplayName("a page size above 100")
        void aPageSizeAbove100() {
            list("?size=101")
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.errors[0].field").isEqualTo("size");
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
                .jsonPath("$.paths['/api/v1/sites'].get").exists()
                .jsonPath("$.paths['/api/v1/sites'].get.parameters[*].name")
                .value(List.class, names -> assertThat(names).contains("page", "size", "sort"));
    }
}
