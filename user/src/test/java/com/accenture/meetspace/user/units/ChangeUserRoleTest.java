package com.accenture.meetspace.user.units;

import com.accenture.meetspace.bootstrap.application.command.InvalidCommandException;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommand;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommandHandler;
import com.accenture.meetspace.user.auth.domain.exceptions.EmailAlreadyTakenException;
import com.accenture.meetspace.user.auth.domain.exceptions.UsernameAlreadyTakenException;
import com.accenture.meetspace.user.profile.application.usecases.command.changerole.ChangeUserRoleCommand;
import com.accenture.meetspace.user.profile.application.usecases.command.changerole.ChangeUserRoleCommandHandler;
import com.accenture.meetspace.user.profile.application.usecases.command.changestatus.ChangeUserStatusCommand;
import com.accenture.meetspace.user.profile.application.usecases.command.changestatus.ChangeUserStatusCommandHandler;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;
import com.accenture.meetspace.user.profile.domain.exceptions.LastActiveAdminException;
import com.accenture.meetspace.user.profile.domain.exceptions.UserNotFoundException;
import com.accenture.meetspace.user.units.stubs.InMemoryIdentityProvider;
import com.accenture.meetspace.user.units.stubs.InMemoryReservationCanceller;
import com.accenture.meetspace.user.units.stubs.InMemoryUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Change user role")
class ChangeUserRoleTest {

    @Nested
    @DisplayName("command")
    class Command {

        @Test
        @DisplayName("rejects a missing user id")
        void rejectsAMissingUserId() {
            assertThatThrownBy(() -> new ChangeUserRoleCommand(null, RoleEnum.ADMIN))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasFieldOrPropertyWithValue("field", "userId");
        }

        @Test
        @DisplayName("rejects a missing role")
        void rejectsAMissingRole() {
            assertThatThrownBy(() -> new ChangeUserRoleCommand(UUID.randomUUID(), null))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasFieldOrPropertyWithValue("field", "role");
        }
    }

    @Nested
    @DisplayName("handler")
    class Handler {

        private final InMemoryUserRepository userRepository = new InMemoryUserRepository();
        private final InMemoryIdentityProvider identityProvider = new InMemoryIdentityProvider();
        private final RegisterEmployeeCommandHandler register =
                new RegisterEmployeeCommandHandler(userRepository, identityProvider);
        private final ChangeUserRoleCommandHandler handler =
                new ChangeUserRoleCommandHandler(userRepository, identityProvider);
        private final ChangeUserStatusCommandHandler status = new ChangeUserStatusCommandHandler(
                userRepository, identityProvider, new InMemoryReservationCanceller());

        private UUID registered(String username) {
            return register.handle(new RegisterEmployeeCommand(UUID.randomUUID(), username,
                    username + "@accenture.com", "Passw0rd!", "John", "Smith")).userId();
        }

        private UUID admin(String username) {
            var userId = registered(username);
            handler.handle(new ChangeUserRoleCommand(userId, RoleEnum.ADMIN));
            return userId;
        }

        private UUID keycloakUserId(String username) {
            return identityProvider.createdUsers().stream()
                    .filter(created -> created.username().equals(username))
                    .findFirst().orElseThrow().keycloakUserId();
        }

        @Nested
        @DisplayName("promotes an employee")
        class Promotes {

            @Test
            @DisplayName("to an admin who keeps the same user")
            void toAnAdminWhoKeepsTheSameUser() {
                var userId = registered("jsmith");

                var response = handler.handle(new ChangeUserRoleCommand(userId, RoleEnum.ADMIN));

                assertThat(response.userId()).isEqualTo(userId);
                assertThat(response.role()).isEqualTo(RoleEnum.ADMIN);
                var admin = userRepository.findAdmin(userId).orElseThrow().snapshot();
                assertThat(admin.role()).isEqualTo(RoleEnum.ADMIN);
                assertThat(admin.user().id()).isEqualTo(userId);
                assertThat(admin.user().username()).isEqualTo("jsmith");
                assertThat(userRepository.findEmployee(userId)).isEmpty();
            }

            @Test
            @DisplayName("in the identity provider too")
            void inTheIdentityProviderToo() {
                var userId = registered("jsmith");

                handler.handle(new ChangeUserRoleCommand(userId, RoleEnum.ADMIN));

                assertThat(identityProvider.roleChanges()).containsExactly(
                        new InMemoryIdentityProvider.RoleChange(keycloakUserId("jsmith"), RoleEnum.ADMIN));
            }
        }

        @Nested
        @DisplayName("demotes an admin")
        class Demotes {

            @Test
            @DisplayName("who is already inactive while another admin stays active")
            void whoIsAlreadyInactiveWhileAnotherAdminStaysActive() {
                var inactive = admin("jsmith");
                var active = admin("akumar");
                status.handle(new ChangeUserStatusCommand(active, inactive, false));

                var response = handler.handle(new ChangeUserRoleCommand(inactive, RoleEnum.EMPLOYEE));

                assertThat(response.role()).isEqualTo(RoleEnum.EMPLOYEE);
                assertThat(userRepository.findEmployee(inactive)).isPresent();
            }

            @Test
            @DisplayName("to an employee when another active admin remains")
            void toAnEmployeeWhenAnotherActiveAdminRemains() {
                var userId = admin("jsmith");
                admin("akumar");

                var response = handler.handle(new ChangeUserRoleCommand(userId, RoleEnum.EMPLOYEE));

                assertThat(response.role()).isEqualTo(RoleEnum.EMPLOYEE);
                var employee = userRepository.findEmployee(userId).orElseThrow().snapshot();
                assertThat(employee.role()).isEqualTo(RoleEnum.EMPLOYEE);
                assertThat(employee.user().username()).isEqualTo("jsmith");
                assertThat(userRepository.findAdmin(userId)).isEmpty();
            }

            @Test
            @DisplayName("in the identity provider too")
            void inTheIdentityProviderToo() {
                var userId = admin("jsmith");
                admin("akumar");

                handler.handle(new ChangeUserRoleCommand(userId, RoleEnum.EMPLOYEE));

                assertThat(identityProvider.roleChanges()).last().isEqualTo(
                        new InMemoryIdentityProvider.RoleChange(keycloakUserId("jsmith"), RoleEnum.EMPLOYEE));
            }
        }

        @Nested
        @DisplayName("changes nothing")
        class ChangesNothing {

            @Test
            @DisplayName("when the user is already an admin")
            void whenTheUserIsAlreadyAnAdmin() {
                var userId = admin("jsmith");
                var changesBefore = identityProvider.roleChanges().size();

                var response = handler.handle(new ChangeUserRoleCommand(userId, RoleEnum.ADMIN));

                assertThat(response.role()).isEqualTo(RoleEnum.ADMIN);
                assertThat(userRepository.findAdmin(userId)).isPresent();
                assertThat(identityProvider.roleChanges()).hasSize(changesBefore);
            }

            @Test
            @DisplayName("when the user is already an employee")
            void whenTheUserIsAlreadyAnEmployee() {
                var userId = registered("jsmith");

                var response = handler.handle(new ChangeUserRoleCommand(userId, RoleEnum.EMPLOYEE));

                assertThat(response.role()).isEqualTo(RoleEnum.EMPLOYEE);
                assertThat(userRepository.findEmployee(userId)).isPresent();
                assertThat(identityProvider.roleChanges()).isEmpty();
            }
        }

        @Nested
        @DisplayName("fails")
        class Fails {

            @Test
            @DisplayName("when the only other admin is inactive")
            void whenTheOnlyOtherAdminIsInactive() {
                var active = admin("jsmith");
                var inactive = admin("akumar");
                status.handle(new ChangeUserStatusCommand(active, inactive, false));

                assertThatThrownBy(() -> handler.handle(new ChangeUserRoleCommand(active, RoleEnum.EMPLOYEE)))
                        .isInstanceOf(LastActiveAdminException.class);
            }

            @Test
            @DisplayName("when the last active admin is demoted")
            void whenTheLastActiveAdminIsDemoted() {
                var userId = admin("jsmith");
                var changesBefore = identityProvider.roleChanges().size();

                assertThatThrownBy(() -> handler.handle(new ChangeUserRoleCommand(userId, RoleEnum.EMPLOYEE)))
                        .isInstanceOf(LastActiveAdminException.class)
                        .hasFieldOrPropertyWithValue("code", "LAST_ACTIVE_ADMIN");
                assertThat(userRepository.findAdmin(userId)).isPresent();
                assertThat(identityProvider.roleChanges()).hasSize(changesBefore);
            }

            @Test
            @DisplayName("when the user to promote does not exist")
            void whenTheUserToPromoteDoesNotExist() {
                assertThatThrownBy(() -> handler.handle(new ChangeUserRoleCommand(UUID.randomUUID(), RoleEnum.ADMIN)))
                        .isInstanceOf(UserNotFoundException.class)
                        .hasFieldOrPropertyWithValue("code", "USER_NOT_FOUND");
            }

            @Test
            @DisplayName("when the user to demote does not exist")
            void whenTheUserToDemoteDoesNotExist() {
                assertThatThrownBy(() -> handler.handle(new ChangeUserRoleCommand(UUID.randomUUID(), RoleEnum.EMPLOYEE)))
                        .isInstanceOf(UserNotFoundException.class)
                        .hasFieldOrPropertyWithValue("code", "USER_NOT_FOUND");
            }
        }

        @Nested
        @DisplayName("keeps the username and email of an admin taken")
        class KeepsAdminIdentityTaken {

            @Test
            @DisplayName("for a new registration with the same username")
            void forANewRegistrationWithTheSameUsername() {
                admin("jsmith");

                assertThatThrownBy(() -> register.handle(new RegisterEmployeeCommand(UUID.randomUUID(), "jsmith",
                        "other@accenture.com", "Passw0rd!", "Jane", "Smith")))
                        .isInstanceOf(UsernameAlreadyTakenException.class);
            }

            @Test
            @DisplayName("for a new registration with the same email")
            void forANewRegistrationWithTheSameEmail() {
                admin("jsmith");

                assertThatThrownBy(() -> register.handle(new RegisterEmployeeCommand(UUID.randomUUID(), "jane",
                        "jsmith@accenture.com", "Passw0rd!", "Jane", "Smith")))
                        .isInstanceOf(EmailAlreadyTakenException.class);
            }
        }
    }
}
