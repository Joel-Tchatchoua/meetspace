package com.accenture.meetspace.room;

import com.accenture.meetspace.bootstrap.application.command.CommandBus;
import com.accenture.meetspace.bootstrap.test.e2e.E2ETest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@E2ETest
@DisplayName("RoomApplication")
class RoomApplicationTest {

    @Autowired
    ApplicationContext context;

    @Test
    @DisplayName("démarre seule avec le socle de bootstrap")
    void startsAloneWithTheBootstrapKernel() {
        assertThat(context.getBean(CommandBus.class)).isNotNull();
        assertThat(context.getBeanNamesForType(RoomApplication.class)).hasSize(1);
    }
}
