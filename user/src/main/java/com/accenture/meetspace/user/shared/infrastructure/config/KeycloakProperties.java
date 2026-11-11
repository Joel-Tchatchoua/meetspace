package com.accenture.meetspace.user.shared.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties("meetspace.keycloak")
public record KeycloakProperties(String baseUrl, String realm, String clientId, String clientSecret,
                                 @DefaultValue("2s") Duration connectTimeout,
                                 @DefaultValue("5s") Duration readTimeout) {

    @Override
    public String toString() {
        return "KeycloakProperties[baseUrl=" + baseUrl + ", realm=" + realm + ", clientId=" + clientId + "]";
    }
}
