package com.accenture.meetspace.bootstrap.app;

import com.accenture.meetspace.bootstrap.test.e2e.E2ETest;
import com.accenture.meetspace.reservation.ReservationApplication;
import com.accenture.meetspace.room.RoomApplication;
import com.accenture.meetspace.user.UserApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@E2ETest
@DisplayName("Assemblage du monolithe")
class MonolithAssemblyE2ETest {

    @Autowired
    ApplicationContext context;

    @Autowired
    RestTestClient client;

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
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isEmpty()
                .jsonPath("$.page.totalElements").isEqualTo(0);
    }
}
