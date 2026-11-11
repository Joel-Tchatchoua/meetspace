package com.accenture.meetspace.user.shared.infrastructure.config;

import com.accenture.meetspace.user.shared.infrastructure.adapters.out.keycloak.KeycloakClient;
import com.accenture.meetspace.user.profile.application.port.out.identity.IdentityProvider;
import com.accenture.meetspace.user.profile.infrastructure.adapters.out.identity.KeycloakIdentityProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty("meetspace.keycloak.base-url")
@EnableConfigurationProperties(KeycloakProperties.class)
public class KeycloakAdaptersConfiguration {

    @Bean
    KeycloakClient keycloakClient(KeycloakProperties properties) {
        return new KeycloakClient(properties);
    }

    @Bean
    IdentityProvider keycloakIdentityProvider(KeycloakClient keycloakClient) {
        return new KeycloakIdentityProvider(keycloakClient);
    }
}
