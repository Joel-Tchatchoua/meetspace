package com.accenture.meetspace.room.e2e;

import com.accenture.meetspace.bootstrap.test.security.TestJwts;
import org.springframework.boot.resttestclient.autoconfigure.RestTestClientBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

@TestConfiguration(proxyBeanMethods = false)
class RoomE2EConfiguration {

    static final String ADMIN = "room-admin";

    @Bean
    RestTestClientBuilderCustomizer actingAsAnAdmin(TestJwts jwts) {
        return builder -> builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwts.admin(ADMIN));
    }
}
