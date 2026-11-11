package com.accenture.meetspace.user.units;

import com.accenture.meetspace.bootstrap.application.command.InvalidCommandException;
import com.accenture.meetspace.user.profile.application.port.out.identity.AuthTokens;
import com.accenture.meetspace.user.auth.application.usecases.command.login.LoginCommand;
import com.accenture.meetspace.user.auth.application.usecases.command.login.LoginCommandHandler;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommand;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommandHandler;
import com.accenture.meetspace.user.auth.domain.enums.LoginStatus;
import com.accenture.meetspace.user.auth.domain.exceptions.AccountDisabledException;
import com.accenture.meetspace.user.auth.domain.exceptions.InvalidCredentialsException;
import com.accenture.meetspace.user.profile.application.usecases.command.changestatus.ChangeUserStatusCommand;
import com.accenture.meetspace.user.profile.application.usecases.command.changestatus.ChangeUserStatusCommandHandler;
import com.accenture.meetspace.user.units.stubs.InMemoryIdentityProvider;
import com.accenture.meetspace.user.units.stubs.InMemoryReservationCanceller;
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

@DisplayName("Login")
class LoginTest {

    private static final String PASSWORD = "Passw0rd!";
    private static final AuthTokens TOKENS = new AuthTokens("access-token", "refresh-token");

    @Nested
    @DisplayName("command")
    class Command {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = "  ")
        @DisplayName("rejects a missing username")
        void rejectsAMissingUsername(String username) {
            assertThatThrownBy(() -> new LoginCommand(username, PASSWORD))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasFieldOrPropertyWithValue("field", "username");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = "  ")
        @DisplayName("rejects a missing password")
        void rejectsAMissingPassword(String password) {
            assertThatThrownBy(() -> new LoginCommand("jsmith", password))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasFieldOrPropertyWithValue("field", "password");
        }

        @Test
        @DisplayName("never exposes the password in its text form")
        void neverExposesThePasswordInItsTextForm() {
            assertThat(new LoginCommand("jsmith", PASSWORD).toString()).doesNotContain(PASSWORD);
        }
    }

    @Nested
    @DisplayName("handler")
    class Handler {

        private final InMemoryUserRepository users = new InMemoryUserRepository();
        private final InMemoryIdentityProvider identityProvider = new InMemoryIdentityProvider();
        private final LoginCommandHandler handler = new LoginCommandHandler(users, identityProvider);

        private UUID registeredJsmith() {
            identityProvider.accept("jsmith", PASSWORD, TOKENS);
            return new RegisterEmployeeCommandHandler(users, identityProvider).handle(new RegisterEmployeeCommand(
                    UUID.randomUUID(), "jsmith", "jsmith@accenture.com", PASSWORD, "John", "Smith")).userId();
        }

        @Test
        @DisplayName("exchanges the username and password for tokens")
        void exchangesTheUsernameAndPasswordForTokens() {
            registeredJsmith();

            var response = handler.handle(new LoginCommand("jsmith", PASSWORD));

            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            assertThat(response.status()).isEqualTo(LoginStatus.AUTHENTICATED);
            assertThat(response.message()).isEqualTo("Authentication successful");
        }

        @Test
        @DisplayName("fails with invalid credentials")
        void failsWithInvalidCredentials() {
            registeredJsmith();

            assertThatThrownBy(() -> handler.handle(new LoginCommand("jsmith", "wrong")))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasFieldOrPropertyWithValue("code", "INVALID_CREDENTIALS");
        }

        @Test
        @DisplayName("fails for an unknown user, without asking for tokens")
        void failsForAnUnknownUser() {
            assertThatThrownBy(() -> handler.handle(new LoginCommand("ghost", PASSWORD)))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasFieldOrPropertyWithValue("code", "INVALID_CREDENTIALS");
            assertThat(identityProvider.exchanges()).isZero();
        }

        @Test
        @DisplayName("fails for a deactivated user, without asking for tokens")
        void failsForADeactivatedUser() {
            var userId = registeredJsmith();
            new ChangeUserStatusCommandHandler(users, identityProvider, new InMemoryReservationCanceller())
                    .handle(new ChangeUserStatusCommand(UUID.randomUUID(), userId, false));

            assertThatThrownBy(() -> handler.handle(new LoginCommand("jsmith", PASSWORD)))
                    .isInstanceOf(AccountDisabledException.class)
                    .hasFieldOrPropertyWithValue("code", "ACCOUNT_DISABLED");
            assertThat(identityProvider.exchanges()).isZero();
        }
    }
}
