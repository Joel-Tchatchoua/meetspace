package com.accenture.meetspace.user.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.HashMap;
import java.util.Map;

@UserE2ETest
@DisplayName("POST /api/v1/auth/register")
class RegisterE2ETest {

    @Autowired
    RestTestClient client;

    @Autowired
    UserFixtures fixtures;

    private static Map<String, String> jsmith() {
        var body = new HashMap<String, String>();
        body.put("username", "jsmith");
        body.put("email", "jsmith@accenture.com");
        body.put("password", UserFixtures.PASSWORD);
        body.put("passwordConfirmation", UserFixtures.PASSWORD);
        body.put("firstName", "John");
        body.put("lastName", "Smith");
        return body;
    }

    private RestTestClient.ResponseSpec register(Map<String, String> body) {
        return client.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange();
    }

    @Test
    @DisplayName("creates an account and returns its id and location")
    void createsAnAccount() {
        register(jsmith())
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody()
                .jsonPath("$.userId").isNotEmpty();
    }

    @Nested
    @DisplayName("rejects with 409")
    class Conflict {

        @Test
        @DisplayName("a username already taken")
        void aUsernameAlreadyTaken() {
            fixtures.registered("jsmith");
            var body = jsmith();
            body.put("email", "other@accenture.com");

            register(body)
                    .expectStatus().isEqualTo(409)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("USERNAME_ALREADY_TAKEN")
                    .jsonPath("$.service").isEqualTo("user")
                    .jsonPath("$.timestamp").isNotEmpty();
        }

        @Test
        @DisplayName("an email already taken")
        void anEmailAlreadyTaken() {
            fixtures.registered("jsmith");
            var body = jsmith();
            body.put("username", "other");

            register(body)
                    .expectStatus().isEqualTo(409)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("EMAIL_ALREADY_TAKEN");
        }
    }

    @Nested
    @DisplayName("rejects with 400")
    class Invalid {

        @Test
        @DisplayName("a confirmation different from the password")
        void aConfirmationDifferentFromThePassword() {
            var body = jsmith();
            body.put("passwordConfirmation", "Different1!");

            register(body)
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.errors[0].field").isEqualTo("passwordConfirmation")
                    .jsonPath("$.errors[0].message").isNotEmpty();
        }

        @Test
        @DisplayName("a weak password")
        void aWeakPassword() {
            var body = jsmith();
            body.put("password", "weak");
            body.put("passwordConfirmation", "weak");

            register(body)
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.errors[0].field").isEqualTo("password");
        }

        @ParameterizedTest
        @ValueSource(strings = {"username", "email", "firstName", "lastName"})
        @DisplayName("a missing field")
        void aMissingField(String field) {
            var body = jsmith();
            body.remove(field);

            register(body)
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.errors[0].field").isEqualTo(field);
        }
    }
}
