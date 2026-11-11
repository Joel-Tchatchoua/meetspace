package com.accenture.meetspace.room.e2e;

import com.accenture.meetspace.bootstrap.test.security.TestJwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@RoomE2ETest
@DisplayName("Security of the room endpoints")
class RoomSecurityE2ETest {

    private static final String ID = UUID.randomUUID().toString();
    private static final Map<String, Object> SITE = Map.of("code", "EBN", "name", "Ebène", "address", "Cybercity");
    private static final Map<String, Object> ROOM = Map.of("code", "R101", "name", "Dodo", "floor", 1, "capacity", 8);

    @Autowired
    RestTestClient client;

    @Autowired
    TestJwts jwts;

    private RestTestClient anonymous;

    @BeforeEach
    void withoutDefaultToken() {
        anonymous = client.mutate().defaultHeaders(headers -> headers.remove(HttpHeaders.AUTHORIZATION)).build();
    }

    static Stream<Arguments> reads() {
        return Stream.of(
                Arguments.of("/api/v1/sites"),
                Arguments.of("/api/v1/sites/" + ID),
                Arguments.of("/api/v1/sites/" + ID + "/rooms"),
                Arguments.of("/api/v1/rooms/" + ID));
    }

    static Stream<Arguments> writes() {
        return Stream.of(
                Arguments.of(HttpMethod.POST, "/api/v1/sites", SITE),
                Arguments.of(HttpMethod.PUT, "/api/v1/sites/" + ID, SITE),
                Arguments.of(HttpMethod.DELETE, "/api/v1/sites/" + ID, null),
                Arguments.of(HttpMethod.POST, "/api/v1/sites/" + ID + "/rooms", ROOM),
                Arguments.of(HttpMethod.PUT, "/api/v1/rooms/" + ID, ROOM),
                Arguments.of(HttpMethod.PATCH, "/api/v1/rooms/" + ID + "/status", Map.of("status", "MAINTENANCE")),
                Arguments.of(HttpMethod.DELETE, "/api/v1/rooms/" + ID, null));
    }

    private RestTestClient.ResponseSpec read(String path, String bearer) {
        var request = anonymous.get().uri(path);
        if (bearer != null) {
            request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        return request.exchange();
    }

    private RestTestClient.ResponseSpec write(HttpMethod method, String path, Object body, String bearer) {
        var request = anonymous.method(method).uri(path);
        if (bearer != null) {
            request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).body(body);
        }
        return request.exchange();
    }

    @Nested
    @DisplayName("reading")
    class Reading {

        @ParameterizedTest(name = "GET {0}")
        @MethodSource("com.accenture.meetspace.room.e2e.RoomSecurityE2ETest#reads")
        @DisplayName("requires a token")
        void requiresAToken(String path) {
            read(path, null)
                    .expectStatus().isUnauthorized()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("UNAUTHENTICATED")
                    .jsonPath("$.service").isEqualTo("meetspace");
        }

        @ParameterizedTest(name = "GET {0}")
        @MethodSource("com.accenture.meetspace.room.e2e.RoomSecurityE2ETest#reads")
        @DisplayName("is open to an employee")
        void isOpenToAnEmployee(String path) {
            var status = read(path, "Bearer " + jwts.employee("jsmith")).returnResult().getStatus().value();

            assertThat(status).isIn(200, 404);
        }

        @ParameterizedTest(name = "GET {0}")
        @MethodSource("com.accenture.meetspace.room.e2e.RoomSecurityE2ETest#reads")
        @DisplayName("is open to the booking service")
        void isOpenToTheBookingService(String path) {
            var status = read(path, "Bearer " + jwts.service("booking-service")).returnResult().getStatus().value();

            assertThat(status).isIn(200, 404);
        }
    }

    @Nested
    @DisplayName("writing")
    class Writing {

        @ParameterizedTest(name = "{0} {1}")
        @MethodSource("com.accenture.meetspace.room.e2e.RoomSecurityE2ETest#writes")
        @DisplayName("requires a token")
        void requiresAToken(HttpMethod method, String path, Object body) {
            write(method, path, body, null)
                    .expectStatus().isUnauthorized()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("UNAUTHENTICATED");
        }

        @ParameterizedTest(name = "{0} {1}")
        @MethodSource("com.accenture.meetspace.room.e2e.RoomSecurityE2ETest#writes")
        @DisplayName("is refused to an employee")
        void isRefusedToAnEmployee(HttpMethod method, String path, Object body) {
            write(method, path, body, "Bearer " + jwts.employee("jsmith"))
                    .expectStatus().isForbidden()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("ACCESS_DENIED")
                    .jsonPath("$.service").isEqualTo("room");
        }

        @ParameterizedTest(name = "{0} {1}")
        @MethodSource("com.accenture.meetspace.room.e2e.RoomSecurityE2ETest#writes")
        @DisplayName("is refused to the booking service")
        void isRefusedToTheBookingService(HttpMethod method, String path, Object body) {
            write(method, path, body, "Bearer " + jwts.service("booking-service"))
                    .expectStatus().isForbidden()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("ACCESS_DENIED");
        }
    }
}
