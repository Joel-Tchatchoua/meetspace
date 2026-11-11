package com.accenture.meetspace.user.e2e;

import com.accenture.meetspace.bootstrap.test.security.TestJwts;
import com.accenture.meetspace.user.units.stubs.InMemoryIdentityProvider;
import com.accenture.meetspace.user.units.stubs.InMemoryReservationCanceller;
import com.accenture.meetspace.user.units.stubs.InMemorySiteCatalog;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
class UserE2EConfiguration {

    @Bean
    InMemoryIdentityProvider inMemoryIdentityProvider() {
        return new InMemoryIdentityProvider();
    }

    @Bean
    InMemorySiteCatalog inMemorySiteCatalog() {
        return new InMemorySiteCatalog();
    }

    @Bean
    InMemoryReservationCanceller inMemoryReservationCanceller() {
        return new InMemoryReservationCanceller();
    }

    @Bean
    UserFixtures userFixtures(ApplicationContext context, TestJwts jwts) {
        return new UserFixtures(context, jwts);
    }
}
