package com.accenture.meetspace.user.units;

import com.accenture.meetspace.bootstrap.application.command.InvalidCommandException;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommand;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommandHandler;
import com.accenture.meetspace.user.profile.application.usecases.command.changestatus.ChangeUserStatusCommand;
import com.accenture.meetspace.user.profile.application.usecases.command.changestatus.ChangeUserStatusCommandHandler;
import com.accenture.meetspace.user.profile.domain.exceptions.CannotDeactivateOwnAccountException;
import com.accenture.meetspace.user.profile.domain.exceptions.UserNotFoundException;
import com.accenture.meetspace.user.units.stubs.InMemoryIdentityProvider;
import com.accenture.meetspace.user.units.stubs.InMemoryReservationCanceller;
import com.accenture.meetspace.user.units.stubs.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Change user status")
class ChangeUserStatusTest {

    private static final UUID ADMIN_ID = UUID.randomUUID();

    @Nested
    @DisplayName("command")
    class Command {

        @Test
        @DisplayName("rejects a missing actor")
        void rejectsAMissingActor() {
            assertThatThrownBy(() -> new ChangeUserStatusCommand(null, UUID.randomUUID(), false))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasFieldOrPropertyWithValue("field", "actorId");
        }

        @Test
        @DisplayName("rejects a missing user id")
        void rejectsAMissingUserId() {
            assertThatThrownBy(() -> new ChangeUserStatusCommand(ADMIN_ID, null, false))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasFieldOrPropertyWithValue("field", "userId");
        }
    }

    @Nested
    @DisplayName("handler")
    class Handler {

        private final InMemoryUserRepository users = new InMemoryUserRepository();
        private final InMemoryIdentityProvider identityProvider = new InMemoryIdentityProvider();
        private final InMemoryReservationCanceller reservations = new InMemoryReservationCanceller();
        private final ChangeUserStatusCommandHandler handler =
                new ChangeUserStatusCommandHandler(users, identityProvider, reservations);

        private UUID userId;
        private UUID keycloakUserId;

        @BeforeEach
        void givenARegisteredUser() {
            userId = new RegisterEmployeeCommandHandler(users, identityProvider).handle(new RegisterEmployeeCommand(
                    UUID.randomUUID(), "jsmith", "jsmith@accenture.com", "Passw0rd!", "John", "Smith")).userId();
            keycloakUserId = identityProvider.createdUsers().getFirst().keycloakUserId();
        }

        @Nested
        @DisplayName("deactivates a user")
        class Deactivates {

            @Test
            @DisplayName("in the profile")
            void inTheProfile() {
                var response = handler.handle(new ChangeUserStatusCommand(ADMIN_ID, userId, false));

                assertThat(response.userId()).isEqualTo(userId);
                assertThat(response.active()).isFalse();
                assertThat(users.findUser(userId).orElseThrow().snapshot().active()).isFalse();
            }

            @Test
            @DisplayName("in the identity provider")
            void inTheIdentityProvider() {
                handler.handle(new ChangeUserStatusCommand(ADMIN_ID, userId, false));

                assertThat(identityProvider.disabledUsers()).containsExactly(keycloakUserId);
            }

            @Test
            @DisplayName("cancelling all their upcoming reservations")
            void cancellingAllTheirUpcomingReservations() {
                handler.handle(new ChangeUserStatusCommand(ADMIN_ID, userId, false));

                assertThat(reservations.cancelledFor()).containsExactly(userId);
            }

            @Test
            @DisplayName("only once when already deactivated")
            void onlyOnceWhenAlreadyDeactivated() {
                handler.handle(new ChangeUserStatusCommand(ADMIN_ID, userId, false));

                handler.handle(new ChangeUserStatusCommand(ADMIN_ID, userId, false));

                assertThat(identityProvider.disabledUsers()).hasSize(1);
                assertThat(reservations.cancelledFor()).hasSize(1);
            }
        }

        @Nested
        @DisplayName("activates a user")
        class Activates {

            @BeforeEach
            void givenADeactivatedUser() {
                handler.handle(new ChangeUserStatusCommand(ADMIN_ID, userId, false));
            }

            @Test
            @DisplayName("in the profile and the identity provider")
            void inTheProfileAndTheIdentityProvider() {
                var response = handler.handle(new ChangeUserStatusCommand(ADMIN_ID, userId, true));

                assertThat(response.active()).isTrue();
                assertThat(users.findUser(userId).orElseThrow().snapshot().active()).isTrue();
                assertThat(identityProvider.enabledUsers()).containsExactly(keycloakUserId);
            }

            @Test
            @DisplayName("only once when already active")
            void onlyOnceWhenAlreadyActive() {
                handler.handle(new ChangeUserStatusCommand(ADMIN_ID, userId, true));

                handler.handle(new ChangeUserStatusCommand(ADMIN_ID, userId, true));

                assertThat(identityProvider.enabledUsers()).hasSize(1);
            }
        }

        @Nested
        @DisplayName("fails")
        class Fails {

            @Test
            @DisplayName("when an admin deactivates their own account")
            void whenAnAdminDeactivatesTheirOwnAccount() {
                assertThatThrownBy(() -> handler.handle(new ChangeUserStatusCommand(userId, userId, false)))
                        .isInstanceOf(CannotDeactivateOwnAccountException.class)
                        .hasFieldOrPropertyWithValue("code", "CANNOT_DEACTIVATE_OWN_ACCOUNT");
                assertThat(users.findUser(userId).orElseThrow().snapshot().active()).isTrue();
                assertThat(identityProvider.disabledUsers()).isEmpty();
                assertThat(reservations.cancelledFor()).isEmpty();
            }

            @Test
            @DisplayName("when the user does not exist")
            void whenTheUserDoesNotExist() {
                assertThatThrownBy(() -> handler.handle(new ChangeUserStatusCommand(ADMIN_ID, UUID.randomUUID(), false)))
                        .isInstanceOf(UserNotFoundException.class)
                        .hasFieldOrPropertyWithValue("code", "USER_NOT_FOUND");
            }
        }
    }
}
