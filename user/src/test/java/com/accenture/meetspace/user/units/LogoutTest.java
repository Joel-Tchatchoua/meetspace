package com.accenture.meetspace.user.units;

import com.accenture.meetspace.bootstrap.application.command.InvalidCommandException;
import com.accenture.meetspace.user.auth.application.usecases.command.logout.LogoutCommand;
import com.accenture.meetspace.user.auth.application.usecases.command.logout.LogoutCommandHandler;
import com.accenture.meetspace.user.units.stubs.InMemoryIdentityProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Logout")
class LogoutTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "  ")
    @DisplayName("rejects a missing refresh token")
    void rejectsAMissingRefreshToken(String refreshToken) {
        assertThatThrownBy(() -> new LogoutCommand(refreshToken))
                .isInstanceOf(InvalidCommandException.class)
                .hasFieldOrPropertyWithValue("field", "refreshToken");
    }

    @Test
    @DisplayName("never exposes the refresh token in its text form")
    void neverExposesTheRefreshTokenInItsTextForm() {
        assertThat(new LogoutCommand("refresh-token").toString()).doesNotContain("refresh-token");
    }

    @Test
    @DisplayName("revokes the refresh token")
    void revokesTheRefreshToken() {
        var identityProvider = new InMemoryIdentityProvider();

        new LogoutCommandHandler(identityProvider).handle(new LogoutCommand("refresh-token"));

        assertThat(identityProvider.revokedTokens()).containsExactly("refresh-token");
    }
}
