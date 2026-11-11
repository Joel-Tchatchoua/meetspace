package com.accenture.meetspace.user.e2e;

import com.accenture.meetspace.bootstrap.application.command.CommandBus;
import com.accenture.meetspace.bootstrap.test.security.TestJwts;
import com.accenture.meetspace.user.profile.application.usecases.command.createinitialadmin.CreateInitialAdminCommand;
import com.accenture.meetspace.user.units.stubs.InMemoryIdentityProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import tools.jackson.databind.JsonNode;

import java.util.Map;
import java.util.UUID;

class UserFixtures {

    static final String PASSWORD = "Passw0rd!";

    private final ApplicationContext context;
    private final TestJwts jwts;

    UserFixtures(ApplicationContext context, TestJwts jwts) {
        this.context = context;
        this.jwts = jwts;
    }

    UUID registered(String username, String firstName, String lastName) {
        var body = client().post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "username", username,
                        "email", username + "@accenture.com",
                        "password", PASSWORD,
                        "passwordConfirmation", PASSWORD,
                        "firstName", firstName,
                        "lastName", lastName))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(JsonNode.class)
                .returnResult()
                .getResponseBody();
        return UUID.fromString(body.get("userId").asString());
    }

    UUID registered(String username) {
        return registered(username, "John", "Smith");
    }

    UUID adminId(String username) {
        var body = client().get()
                .uri("/api/v1/profile")
                .header(HttpHeaders.AUTHORIZATION, adminBearer(username))
                .exchange()
                .expectStatus().isOk()
                .expectBody(JsonNode.class)
                .returnResult()
                .getResponseBody();
        return UUID.fromString(body.get("id").asString());
    }

    String adminBearer(String username) {
        var identityProvider = context.getBean(InMemoryIdentityProvider.class);
        if (identityProvider.findUser(username).isEmpty()) {
            identityProvider.addExisting(username, username + "@accenture.com", "Test", username);
        }
        context.getBean(CommandBus.class).dispatch(new CreateInitialAdminCommand(username));
        return bearer(jwts.admin(username));
    }

    String employeeBearer(String username) {
        return bearer(jwts.employee(username));
    }

    String expiredBearer(String username) {
        return bearer(jwts.expired(username));
    }

    private RestTestClient client() {
        return context.getBean(RestTestClient.class);
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
