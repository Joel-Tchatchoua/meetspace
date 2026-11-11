package com.accenture.meetspace.bootstrap.app;

import com.accenture.meetspace.bootstrap.test.e2e.E2ETest;
import com.accenture.meetspace.bootstrap.test.security.TestJwtConfiguration;
import com.accenture.meetspace.bootstrap.test.security.TestJwts;
import com.accenture.meetspace.user.profile.application.port.out.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@E2ETest
@Import(TestJwtConfiguration.class)
@DisplayName("Sécurité partagée entre les modules du monolithe")
class MonolithSecurityE2ETest {

    @Autowired
    RestTestClient client;

    @Autowired
    TestJwts jwts;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PlatformTransactionManager transactionManager;

    private MonolithUsers users;

    @BeforeEach
    void givenUsers() {
        users = new MonolithUsers(userRepository, transactionManager);
    }

    private RestTestClient.ResponseSpec listSites(String token) {
        return client.get()
                .uri("/api/v1/sites")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange();
    }

    @Test
    @DisplayName("un employé actif lit les sites du module room")
    void anActiveEmployeeReadsTheSites() {
        users.employee("jsmith");

        listSites(jwts.employee("jsmith"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.page.totalElements").isEqualTo(0);
    }

    @Test
    @DisplayName("un employé désactivé est bloqué sur room, même avec un token valide")
    void aDeactivatedEmployeeIsBlockedOnRoom() {
        users.deactivate(users.employee("jsmith"));

        listSites(jwts.employee("jsmith"))
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.code").isEqualTo("ACCOUNT_DISABLED");
    }

    @Test
    @DisplayName("un token sans profil est bloqué sur room")
    void aTokenWithoutProfileIsBlockedOnRoom() {
        listSites(jwts.employee("ghost"))
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.code").isEqualTo("PROFILE_NOT_FOUND");
    }

    @Test
    @DisplayName("un employé ne peut pas créer de site")
    void anEmployeeCannotCreateASite() {
        users.employee("jsmith");

        client.post()
                .uri("/api/v1/sites")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwts.employee("jsmith"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("code", "EBN", "name", "Ebène", "address", "Cybercity"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.code").isEqualTo("ACCESS_DENIED");
    }

    @Test
    @DisplayName("une route inconnue de l'API est refusée sans token")
    void anUnknownApiRouteIsRefusedWithoutToken() {
        client.get()
                .uri("/api/v1/unknown")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("UNAUTHENTICATED");
    }

    @Test
    @DisplayName("la santé reste publique")
    void healthStaysPublic() {
        var status = client.get().uri("/actuator/health/liveness").exchange().returnResult().getStatus().value();

        assertThat(status).isNotIn(401, 403);
    }

    @Test
    @DisplayName("l'inscription reste publique")
    void registrationStaysPublic() {
        var status = client.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of())
                .exchange()
                .returnResult()
                .getStatus()
                .value();

        assertThat(status).isEqualTo(400);
    }

    @Test
    @DisplayName("un appel sans token est refusé sur room")
    void aCallWithoutTokenIsRefusedOnRoom() {
        client.get()
                .uri("/api/v1/sites")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("UNAUTHENTICATED");
    }
}
