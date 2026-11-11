package com.accenture.meetspace.bootstrap.app;

import com.accenture.meetspace.bootstrap.test.e2e.E2ETest;
import com.accenture.meetspace.bootstrap.test.security.TestJwtConfiguration;
import com.accenture.meetspace.bootstrap.test.security.TestJwts;
import com.accenture.meetspace.user.profile.application.port.out.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.PlatformTransactionManager;
import com.accenture.meetspace.reservation.ReservationApplication;
import com.accenture.meetspace.room.RoomApplication;
import com.accenture.meetspace.room.application.usecases.command.create.CreateSiteResponse;
import com.accenture.meetspace.user.UserApplication;
import com.accenture.meetspace.user.profile.application.port.out.site.SiteCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@E2ETest
@Import(TestJwtConfiguration.class)
@DisplayName("Assemblage du monolithe")
class MonolithAssemblyE2ETest {

    @Autowired
    ApplicationContext context;

    @Autowired
    RestTestClient client;

    @Autowired
    SiteCatalog siteCatalog;

    @Autowired
    TestJwts jwts;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PlatformTransactionManager transactionManager;

    private String admin;

    @BeforeEach
    void givenAnAdmin() {
        new MonolithUsers(userRepository, transactionManager).admin("root");
        admin = "Bearer " + jwts.admin("root");
    }

    @Test
    @DisplayName("charge tous les modules dans un seul contexte")
    void loadsEveryModule() {
        assertThat(context.getBean(RoomApplication.class)).isNotNull();
        assertThat(context.getBean(ReservationApplication.class)).isNotNull();
        assertThat(context.getBean(UserApplication.class)).isNotNull();
    }

    @Test
    @DisplayName("expose les endpoints du module room")
    void exposesRoomEndpoints() {
        client.get()
                .uri("/api/v1/sites")
                .header(HttpHeaders.AUTHORIZATION, admin)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isEmpty()
                .jsonPath("$.page.totalElements").isEqualTo(0);
    }

    @Nested
    @DisplayName("relie le port SiteCatalog du module user au module room")
    class LinksUserToRoom {

        @Test
        @DisplayName("un site existant est reconnu")
        void anExistingSiteIsKnown() {
            var site = client.post()
                    .uri("/api/v1/sites")
                    .header(HttpHeaders.AUTHORIZATION, admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("code", "EBN", "name", "Ebène", "address", "Cybercity, Ebène"))
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(CreateSiteResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(siteCatalog.exists(site.siteId())).isTrue();
        }

        @Test
        @DisplayName("un site inconnu ne l'est pas")
        void anUnknownSiteIsNotKnown() {
            assertThat(siteCatalog.exists(UUID.randomUUID())).isFalse();
        }
    }
}
