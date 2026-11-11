package com.accenture.meetspace.user.units;

import com.accenture.meetspace.bootstrap.application.command.InvalidCommandException;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommand;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommandHandler;
import com.accenture.meetspace.user.auth.domain.exceptions.EmailAlreadyTakenException;
import com.accenture.meetspace.user.auth.domain.exceptions.UsernameAlreadyTakenException;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;
import com.accenture.meetspace.user.units.stubs.InMemoryUserRepository;
import com.accenture.meetspace.user.units.stubs.InMemoryIdentityProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Register employee")
class RegisterEmployeeTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String STRONG_PASSWORD = "Passw0rd!";

    private static RegisterEmployeeCommand jsmith(UUID userId) {
        return new RegisterEmployeeCommand(
                userId, "jsmith", "jsmith@accenture.com", STRONG_PASSWORD, "John", "Smith");
    }

    @Nested
    @DisplayName("command")
    class Command {

        @Test
        @DisplayName("rejects a missing user id")
        void rejectsAMissingUserId() {
            assertThatThrownBy(() -> jsmith(null))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasFieldOrPropertyWithValue("field", "userId");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = "  ")
        @DisplayName("rejects a missing username")
        void rejectsAMissingUsername(String username) {
            assertThatThrownBy(() -> new RegisterEmployeeCommand(
                    USER_ID, username, "jsmith@accenture.com", STRONG_PASSWORD, "John", "Smith"))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasFieldOrPropertyWithValue("field", "username");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = "  ")
        @DisplayName("rejects a missing email")
        void rejectsAMissingEmail(String email) {
            assertThatThrownBy(() -> new RegisterEmployeeCommand(
                    USER_ID, "jsmith", email, STRONG_PASSWORD, "John", "Smith"))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasFieldOrPropertyWithValue("field", "email");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = "  ")
        @DisplayName("rejects a missing first name")
        void rejectsAMissingFirstName(String firstName) {
            assertThatThrownBy(() -> new RegisterEmployeeCommand(
                    USER_ID, "jsmith", "jsmith@accenture.com", STRONG_PASSWORD, firstName, "Smith"))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasFieldOrPropertyWithValue("field", "firstName");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = "  ")
        @DisplayName("rejects a missing last name")
        void rejectsAMissingLastName(String lastName) {
            assertThatThrownBy(() -> new RegisterEmployeeCommand(
                    USER_ID, "jsmith", "jsmith@accenture.com", STRONG_PASSWORD, "John", lastName))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasFieldOrPropertyWithValue("field", "lastName");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"Pa0!", "Passw0!", "password1!", "Password!", "Password1", "Password1 "})
        @DisplayName("rejects a password that is not strong")
        void rejectsAPasswordThatIsNotStrong(String password) {
            assertThatThrownBy(() -> new RegisterEmployeeCommand(
                    USER_ID, "jsmith", "jsmith@accenture.com", password, "John", "Smith"))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasFieldOrPropertyWithValue("field", "password");
        }

        @ParameterizedTest
        @ValueSource(strings = {"Passw0r!", "PASSW0RD!", "Str0ng-Pass word"})
        @DisplayName("accepts a password of at least 8 characters with an upper-case letter, a digit and a symbol")
        void acceptsAStrongPassword(String password) {
            var command = new RegisterEmployeeCommand(
                    USER_ID, "jsmith", "jsmith@accenture.com", password, "John", "Smith");

            assertThat(command.password()).isEqualTo(password);
        }

        @Test
        @DisplayName("never exposes the password in its text form")
        void neverExposesThePasswordInItsTextForm() {
            assertThat(jsmith(USER_ID).toString()).doesNotContain(STRONG_PASSWORD);
        }
    }

    @Nested
    @DisplayName("handler")
    class Handler {

        private final InMemoryUserRepository userRepository = new InMemoryUserRepository();
        private final InMemoryIdentityProvider identityProvider = new InMemoryIdentityProvider();
        private final RegisterEmployeeCommandHandler handler =
                new RegisterEmployeeCommandHandler(userRepository, identityProvider);

        @Nested
        @DisplayName("succeeds")
        class Succeeds {

            @Test
            @DisplayName("returning the id of the new user")
            void returningTheIdOfTheNewUser() {
                assertThat(handler.handle(jsmith(USER_ID)).userId()).isEqualTo(USER_ID);
            }

            @Test
            @DisplayName("creating the user in the identity provider with the EMPLOYEE role")
            void creatingTheUserInTheIdentityProviderWithTheEmployeeRole() {
                handler.handle(jsmith(USER_ID));

                assertThat(identityProvider.createdUsers())
                        .singleElement()
                        .satisfies(created -> {
                            assertThat(created.username()).isEqualTo("jsmith");
                            assertThat(created.email()).isEqualTo("jsmith@accenture.com");
                            assertThat(created.password()).isEqualTo(STRONG_PASSWORD);
                            assertThat(created.firstName()).isEqualTo("John");
                            assertThat(created.lastName()).isEqualTo("Smith");
                            assertThat(created.role()).isEqualTo(RoleEnum.EMPLOYEE);
                        });
            }

            @Test
            @DisplayName("creating an active employee linked to the identity provider user")
            void creatingAnActiveEmployeeLinkedToTheIdentityProviderUser() {
                handler.handle(jsmith(USER_ID));

                var employee = userRepository.findEmployee(USER_ID).orElseThrow().snapshot();
                assertThat(employee.role()).isEqualTo(RoleEnum.EMPLOYEE);
                assertThat(employee.user().id()).isEqualTo(USER_ID);
                assertThat(employee.user().keycloakUserId())
                        .isEqualTo(identityProvider.createdUsers().getFirst().keycloakUserId());
                assertThat(employee.user().username()).isEqualTo("jsmith");
                assertThat(employee.user().email()).isEqualTo("jsmith@accenture.com");
                assertThat(employee.user().firstName()).isEqualTo("John");
                assertThat(employee.user().lastName()).isEqualTo("Smith");
                assertThat(employee.user().active()).isTrue();
            }
        }

        @Nested
        @DisplayName("fails")
        class Fails {

            @Test
            @DisplayName("when the username is already taken")
            void whenTheUsernameIsAlreadyTaken() {
                handler.handle(jsmith(USER_ID));
                var sameUsername = new RegisterEmployeeCommand(
                        UUID.randomUUID(), "jsmith", "other@accenture.com", STRONG_PASSWORD, "Jane", "Smith");

                assertThatThrownBy(() -> handler.handle(sameUsername))
                        .isInstanceOf(UsernameAlreadyTakenException.class)
                        .hasFieldOrPropertyWithValue("code", "USERNAME_ALREADY_TAKEN");
                assertThat(identityProvider.createdUsers()).hasSize(1);
                assertThat(userRepository.count()).isEqualTo(1);
            }

            @Test
            @DisplayName("when the email is already taken")
            void whenTheEmailIsAlreadyTaken() {
                handler.handle(jsmith(USER_ID));
                var sameEmail = new RegisterEmployeeCommand(
                        UUID.randomUUID(), "jane", "jsmith@accenture.com", STRONG_PASSWORD, "Jane", "Smith");

                assertThatThrownBy(() -> handler.handle(sameEmail))
                        .isInstanceOf(EmailAlreadyTakenException.class)
                        .hasFieldOrPropertyWithValue("code", "EMAIL_ALREADY_TAKEN");
                assertThat(identityProvider.createdUsers()).hasSize(1);
                assertThat(userRepository.count()).isEqualTo(1);
            }

            @Test
            @DisplayName("deleting the identity provider user when saving the profile fails")
            void deletingTheIdentityProviderUserWhenSavingTheProfileFails() {
                var saveFailure = new IllegalStateException("database down");
                userRepository.failOnSaveWith(saveFailure);

                assertThatThrownBy(() -> handler.handle(jsmith(USER_ID))).isSameAs(saveFailure);

                assertThat(identityProvider.deletedUsers())
                        .containsExactly(identityProvider.createdUsers().getFirst().keycloakUserId());
                assertThat(userRepository.count()).isZero();
            }
        }
    }
}
