package com.accenture.meetspace.user.integration;

import com.accenture.meetspace.user.shared.infrastructure.config.KeycloakAdaptersConfiguration;
import com.accenture.meetspace.user.shared.infrastructure.config.UserDefaultAdaptersAutoConfiguration;
import com.accenture.meetspace.user.profile.application.port.out.identity.IdentityProvider;
import com.accenture.meetspace.user.profile.infrastructure.adapters.out.identity.KeycloakIdentityProvider;
import com.accenture.meetspace.user.profile.infrastructure.adapters.out.identity.UnavailableIdentityProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Keycloak adapters wiring")
class KeycloakAdaptersWiringTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(KeycloakAdaptersConfiguration.class)
            .withConfiguration(AutoConfigurations.of(UserDefaultAdaptersAutoConfiguration.class));

    @Test
    @DisplayName("uses Keycloak when it is configured")
    void usesKeycloakWhenItIsConfigured() {
        runner.withPropertyValues(
                        "meetspace.keycloak.base-url=http://localhost:8180",
                        "meetspace.keycloak.realm=meetspace",
                        "meetspace.keycloak.client-id=backend",
                        "meetspace.keycloak.client-secret=s3cret")
                .run(context -> {
                    assertThat(context.getBean(IdentityProvider.class)).isInstanceOf(KeycloakIdentityProvider.class);
                });
    }

    @Test
    @DisplayName("stays unavailable when it is not configured")
    void staysUnavailableWhenItIsNotConfigured() {
        runner.run(context -> {
            assertThat(context.getBean(IdentityProvider.class)).isInstanceOf(UnavailableIdentityProvider.class);
        });
    }
}
