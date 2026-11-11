package com.accenture.meetspace.user.integration;

import com.accenture.meetspace.user.profile.application.port.out.repository.UserRepository;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;
import com.accenture.meetspace.user.units.stubs.InMemoryIdentityProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "meetspace.initial-admin.username=initial-admin")
@ActiveProfiles("test")
@Import(InitialAdminStartupTest.KeycloakKnowsTheInitialAdmin.class)
@DisplayName("Initial admin at startup")
@Transactional
class InitialAdminStartupTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class KeycloakKnowsTheInitialAdmin {

        @Bean
        InMemoryIdentityProvider inMemoryIdentityProvider() {
            var identityProvider = new InMemoryIdentityProvider();
            identityProvider.addExisting("initial-admin", "initial-admin@meetspace.local", "MeetSpace", "Admin");
            return identityProvider;
        }
    }

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("creates the profile of the configured admin")
    void createsTheProfileOfTheConfiguredAdmin() {
        var user = userRepository.findByUsername("initial-admin").orElseThrow().snapshot();

        assertThat(userRepository.findAdmin(user.id()).orElseThrow().snapshot().role()).isEqualTo(RoleEnum.ADMIN);
        assertThat(user.email()).isEqualTo("initial-admin@meetspace.local");
    }
}
