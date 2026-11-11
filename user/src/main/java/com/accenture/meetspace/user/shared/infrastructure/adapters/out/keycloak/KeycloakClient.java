package com.accenture.meetspace.user.shared.infrastructure.adapters.out.keycloak;

import com.accenture.meetspace.user.shared.infrastructure.config.KeycloakProperties;
import com.accenture.meetspace.user.profile.domain.exceptions.ExternalServiceUnavailableException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.util.function.Supplier;

public class KeycloakClient {

    private static final String TOKEN_PATH = "/realms/{realm}/protocol/openid-connect/token";
    private static final String LOGOUT_PATH = "/realms/{realm}/protocol/openid-connect/logout";
    private static final String ADMIN_PATH = "/admin/realms/{realm}";

    public record TokenResponse(@JsonProperty("access_token") String accessToken,
                                @JsonProperty("refresh_token") String refreshToken) {
    }

    private final KeycloakProperties properties;
    private final RestClient http;

    public KeycloakClient(KeycloakProperties properties) {
        this.properties = properties;
        var requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(properties.connectTimeout()).build());
        requestFactory.setReadTimeout(properties.readTimeout());
        this.http = RestClient.builder().baseUrl(properties.baseUrl()).requestFactory(requestFactory).build();
    }

    public TokenResponse passwordGrant(String username, String password) {
        var form = clientForm();
        form.add("grant_type", "password");
        form.add("username", username);
        form.add("password", password);
        return requestToken(form);
    }

    public void logout(String refreshToken) {
        var form = clientForm();
        form.add("refresh_token", refreshToken);
        http.post()
                .uri(LOGOUT_PATH, properties.realm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();
    }

    public RestClient.RequestBodySpec admin(HttpMethod method, String path, Object... variables) {
        var uriVariables = new Object[variables.length + 1];
        uriVariables[0] = properties.realm();
        System.arraycopy(variables, 0, uriVariables, 1, variables.length);
        return http.method(method)
                .uri(ADMIN_PATH + path, uriVariables)
                .headers(headers -> headers.setBearerAuth(serviceToken()));
    }

    public <T> T call(Supplier<T> request) {
        try {
            return request.get();
        } catch (RestClientException e) {
            throw new ExternalServiceUnavailableException("Keycloak");
        }
    }

    public void run(Runnable request) {
        call(() -> {
            request.run();
            return null;
        });
    }

    private String serviceToken() {
        var form = clientForm();
        form.add("grant_type", "client_credentials");
        return requestToken(form).accessToken();
    }

    private TokenResponse requestToken(MultiValueMap<String, String> form) {
        return http.post()
                .uri(TOKEN_PATH, properties.realm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(TokenResponse.class);
    }

    private MultiValueMap<String, String> clientForm() {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());
        return form;
    }
}
