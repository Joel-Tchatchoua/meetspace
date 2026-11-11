package com.accenture.meetspace.user.units;

import com.accenture.meetspace.bootstrap.application.query.InvalidQueryException;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommand;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommandHandler;
import com.accenture.meetspace.user.profile.application.usecases.command.changerole.ChangeUserRoleCommand;
import com.accenture.meetspace.user.profile.application.usecases.command.changerole.ChangeUserRoleCommandHandler;
import com.accenture.meetspace.user.profile.application.usecases.command.updateprofile.UpdateProfileCommand;
import com.accenture.meetspace.user.profile.application.usecases.command.updateprofile.UpdateProfileCommandHandler;
import com.accenture.meetspace.user.profile.application.usecases.query.user.GetUserQuery;
import com.accenture.meetspace.user.profile.application.usecases.query.user.GetUserQueryHandler;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;
import com.accenture.meetspace.user.profile.domain.exceptions.UserNotFoundException;
import com.accenture.meetspace.user.units.stubs.InMemoryIdentityProvider;
import com.accenture.meetspace.user.units.stubs.InMemorySiteCatalog;
import com.accenture.meetspace.user.units.stubs.InMemoryUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Get user")
class GetUserTest {

    @Nested
    @DisplayName("query")
    class Query {

        @Test
        @DisplayName("rejects a missing user id")
        void rejectsAMissingUserId() {
            assertThatThrownBy(() -> new GetUserQuery(null))
                    .isInstanceOf(InvalidQueryException.class)
                    .hasFieldOrPropertyWithValue("field", "userId");
        }
    }

    @Nested
    @DisplayName("handler")
    class Handler {

        private final InMemoryUserRepository users = new InMemoryUserRepository();
        private final InMemoryIdentityProvider identityProvider = new InMemoryIdentityProvider();
        private final GetUserQueryHandler handler = new GetUserQueryHandler(users);

        private UUID registeredJsmith() {
            return new RegisterEmployeeCommandHandler(users, identityProvider).handle(new RegisterEmployeeCommand(
                    UUID.randomUUID(), "jsmith", "jsmith@accenture.com", "Passw0rd!", "John", "Smith")).userId();
        }

        @Test
        @DisplayName("returns the whole profile of an employee")
        void returnsTheWholeProfileOfAnEmployee() {
            var userId = registeredJsmith();
            var siteId = UUID.randomUUID();
            var sites = new InMemorySiteCatalog();
            sites.add(siteId);
            new UpdateProfileCommandHandler(users, sites).handle(
                    new UpdateProfileCommand(userId, null, null, "Engineering", "+230 5555 0000", siteId));

            var profile = handler.handle(new GetUserQuery(userId));

            assertThat(profile.id()).isEqualTo(userId);
            assertThat(profile.username()).isEqualTo("jsmith");
            assertThat(profile.email()).isEqualTo("jsmith@accenture.com");
            assertThat(profile.firstName()).isEqualTo("John");
            assertThat(profile.lastName()).isEqualTo("Smith");
            assertThat(profile.department()).isEqualTo("Engineering");
            assertThat(profile.phone()).isEqualTo("+230 5555 0000");
            assertThat(profile.defaultSiteId()).isEqualTo(siteId);
            assertThat(profile.role()).isEqualTo(RoleEnum.EMPLOYEE);
            assertThat(profile.active()).isTrue();
        }

        @Test
        @DisplayName("returns the role of an admin")
        void returnsTheRoleOfAnAdmin() {
            var userId = registeredJsmith();
            new ChangeUserRoleCommandHandler(users, identityProvider)
                    .handle(new ChangeUserRoleCommand(userId, RoleEnum.ADMIN));

            assertThat(handler.handle(new GetUserQuery(userId)).role()).isEqualTo(RoleEnum.ADMIN);
        }

        @Test
        @DisplayName("fails when the user does not exist")
        void failsWhenTheUserDoesNotExist() {
            assertThatThrownBy(() -> handler.handle(new GetUserQuery(UUID.randomUUID())))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "USER_NOT_FOUND");
        }
    }
}
