package com.accenture.meetspace.bootstrap.app;

import com.accenture.meetspace.user.profile.application.port.out.identity.IdentityProvider;
import com.accenture.meetspace.user.profile.infrastructure.adapters.out.identity.KeycloakIdentityProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class MeetspaceApplicationTests {

    @Autowired
    ObjectProvider<JwtDecoder> jwtDecoder;

    @Autowired
    IdentityProvider identityProvider;

    @Test
    void contextLoads() {
    }

    @Test
    @DisplayName("le profil par défaut valide les tokens de Keycloak")
    void defaultProfileValidatesKeycloakTokens() {
        assertThat(jwtDecoder.getIfAvailable()).isNotNull();
    }

    @Test
    @DisplayName("le profil par défaut utilise les adaptateurs Keycloak")
    void defaultProfileUsesKeycloakAdapters() {
        assertThat(identityProvider).isInstanceOf(KeycloakIdentityProvider.class);
    }
}
