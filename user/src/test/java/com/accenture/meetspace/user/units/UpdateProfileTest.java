package com.accenture.meetspace.user.units;

import com.accenture.meetspace.bootstrap.application.command.InvalidCommandException;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommand;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommandHandler;
import com.accenture.meetspace.user.profile.application.usecases.command.updateprofile.UpdateProfileCommand;
import com.accenture.meetspace.user.profile.application.usecases.command.updateprofile.UpdateProfileCommandHandler;
import com.accenture.meetspace.user.profile.domain.entities.User;
import com.accenture.meetspace.user.profile.domain.exceptions.DefaultSiteNotFoundException;
import com.accenture.meetspace.user.profile.domain.exceptions.UserNotFoundException;
import com.accenture.meetspace.user.units.stubs.InMemoryIdentityProvider;
import com.accenture.meetspace.user.units.stubs.InMemorySiteCatalog;
import com.accenture.meetspace.user.units.stubs.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Update profile")
class UpdateProfileTest {

    private static final UUID SITE_ID = UUID.randomUUID();

    @Nested
    @DisplayName("command")
    class Command {

        @Test
        @DisplayName("rejects a missing user id")
        void rejectsAMissingUserId() {
            assertThatThrownBy(() -> new UpdateProfileCommand(null, null, null, null, null, null))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasFieldOrPropertyWithValue("field", "userId");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "  "})
        @DisplayName("rejects a blank first name when it is sent")
        void rejectsABlankFirstName(String firstName) {
            assertThatThrownBy(() -> new UpdateProfileCommand(UUID.randomUUID(), firstName, null, null, null, null))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasFieldOrPropertyWithValue("field", "firstName");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "  "})
        @DisplayName("rejects a blank last name when it is sent")
        void rejectsABlankLastName(String lastName) {
            assertThatThrownBy(() -> new UpdateProfileCommand(UUID.randomUUID(), null, lastName, null, null, null))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasFieldOrPropertyWithValue("field", "lastName");
        }
    }

    @Nested
    @DisplayName("handler")
    class Handler {

        private final InMemoryUserRepository users = new InMemoryUserRepository();
        private final InMemorySiteCatalog sites = new InMemorySiteCatalog();
        private final UpdateProfileCommandHandler handler = new UpdateProfileCommandHandler(users, sites);

        private UUID userId;

        @BeforeEach
        void givenARegisteredUser() {
            sites.add(SITE_ID);
            userId = new RegisterEmployeeCommandHandler(users, new InMemoryIdentityProvider())
                    .handle(new RegisterEmployeeCommand(UUID.randomUUID(), "jsmith", "jsmith@accenture.com",
                            "Passw0rd!", "John", "Smith"))
                    .userId();
        }

        private User.Snapshot profile() {
            return users.findUser(userId).orElseThrow().snapshot();
        }

        @Test
        @DisplayName("changes every field that is sent")
        void changesEveryFieldThatIsSent() {
            var response = handler.handle(
                    new UpdateProfileCommand(userId, "Johnny", "Smithson", "Engineering", "+230 5555 0000", SITE_ID));

            assertThat(response.userId()).isEqualTo(userId);
            assertThat(profile().firstName()).isEqualTo("Johnny");
            assertThat(profile().lastName()).isEqualTo("Smithson");
            assertThat(profile().department()).isEqualTo("Engineering");
            assertThat(profile().phone()).isEqualTo("+230 5555 0000");
            assertThat(profile().defaultSiteId()).isEqualTo(SITE_ID);
        }

        @Test
        @DisplayName("leaves the fields that are not sent unchanged")
        void leavesTheFieldsThatAreNotSentUnchanged() {
            handler.handle(
                    new UpdateProfileCommand(userId, "Johnny", "Smithson", "Engineering", "+230 5555 0000", SITE_ID));

            handler.handle(new UpdateProfileCommand(userId, null, null, "Sales", null, null));

            assertThat(profile().firstName()).isEqualTo("Johnny");
            assertThat(profile().lastName()).isEqualTo("Smithson");
            assertThat(profile().department()).isEqualTo("Sales");
            assertThat(profile().phone()).isEqualTo("+230 5555 0000");
            assertThat(profile().defaultSiteId()).isEqualTo(SITE_ID);
        }

        @Test
        @DisplayName("fails when the default site does not exist, changing nothing")
        void failsWhenTheDefaultSiteDoesNotExist() {
            var unknownSite = UUID.randomUUID();

            assertThatThrownBy(() -> handler.handle(
                    new UpdateProfileCommand(userId, "Johnny", null, null, null, unknownSite)))
                    .isInstanceOf(DefaultSiteNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "DEFAULT_SITE_NOT_FOUND");
            assertThat(profile().firstName()).isEqualTo("John");
            assertThat(profile().defaultSiteId()).isNull();
        }

        @Test
        @DisplayName("fails when the user does not exist")
        void failsWhenTheUserDoesNotExist() {
            assertThatThrownBy(() -> handler.handle(
                    new UpdateProfileCommand(UUID.randomUUID(), "Johnny", null, null, null, null)))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "USER_NOT_FOUND");
        }
    }
}
