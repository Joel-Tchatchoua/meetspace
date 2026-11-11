package com.accenture.meetspace.user.units;

import com.accenture.meetspace.bootstrap.application.command.InvalidCommandException;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommand;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommandHandler;
import com.accenture.meetspace.user.profile.application.usecases.command.createinitialadmin.CreateInitialAdminCommand;
import com.accenture.meetspace.user.profile.application.usecases.command.createinitialadmin.CreateInitialAdminCommandHandler;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;
import com.accenture.meetspace.user.profile.domain.exceptions.InitialAdminNotFoundException;
import com.accenture.meetspace.user.units.stubs.InMemoryIdentityProvider;
import com.accenture.meetspace.user.units.stubs.InMemoryUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Create initial admin")
class CreateInitialAdminTest {

    @Nested
    @DisplayName("command")
    class Command {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = "  ")
        @DisplayName("rejects a missing username")
        void rejectsAMissingUsername(String username) {
            assertThatThrownBy(() -> new CreateInitialAdminCommand(username))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasFieldOrPropertyWithValue("field", "username");
        }
    }

    @Nested
    @DisplayName("handler")
    class Handler {

        private final InMemoryUserRepository users = new InMemoryUserRepository();
        private final InMemoryIdentityProvider identityProvider = new InMemoryIdentityProvider();
        private final CreateInitialAdminCommandHandler handler =
                new CreateInitialAdminCommandHandler(users, identityProvider);

        @Test
        @DisplayName("creates an active admin from the Keycloak account")
        void createsAnActiveAdminFromTheKeycloakAccount() {
            var keycloakUserId = identityProvider.addExisting("root", "root@meetspace.local", "MeetSpace", "Admin");

            var response = handler.handle(new CreateInitialAdminCommand("root"));

            assertThat(response.created()).isTrue();
            var admin = users.findAdmin(response.userId()).orElseThrow().snapshot();
            assertThat(admin.role()).isEqualTo(RoleEnum.ADMIN);
            assertThat(admin.user().keycloakUserId()).isEqualTo(keycloakUserId);
            assertThat(admin.user().username()).isEqualTo("root");
            assertThat(admin.user().email()).isEqualTo("root@meetspace.local");
            assertThat(admin.user().firstName()).isEqualTo("MeetSpace");
            assertThat(admin.user().lastName()).isEqualTo("Admin");
            assertThat(admin.user().active()).isTrue();
        }

        @Test
        @DisplayName("keeps the existing profile on the next start")
        void keepsTheExistingProfileOnTheNextStart() {
            identityProvider.addExisting("root", "root@meetspace.local", "MeetSpace", "Admin");
            var first = handler.handle(new CreateInitialAdminCommand("root"));

            var second = handler.handle(new CreateInitialAdminCommand("root"));

            assertThat(second.created()).isFalse();
            assertThat(second.userId()).isEqualTo(first.userId());
            assertThat(users.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("keeps a profile registered with the same username")
        void keepsAProfileRegisteredWithTheSameUsername() {
            var userId = new RegisterEmployeeCommandHandler(users, identityProvider).handle(new RegisterEmployeeCommand(
                    UUID.randomUUID(), "root", "root@meetspace.local", "Passw0rd!", "John", "Smith")).userId();

            var response = handler.handle(new CreateInitialAdminCommand("root"));

            assertThat(response.created()).isFalse();
            assertThat(response.userId()).isEqualTo(userId);
            assertThat(users.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("fails when Keycloak has no such account")
        void failsWhenKeycloakHasNoSuchAccount() {
            assertThatThrownBy(() -> handler.handle(new CreateInitialAdminCommand("root")))
                    .isInstanceOf(InitialAdminNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "INITIAL_ADMIN_NOT_FOUND");
            assertThat(users.count()).isZero();
        }
    }
}
