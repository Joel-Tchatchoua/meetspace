package com.accenture.meetspace.user.units;

import com.accenture.meetspace.bootstrap.application.query.Pagination;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommand;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommandHandler;
import com.accenture.meetspace.user.profile.application.usecases.command.changerole.ChangeUserRoleCommand;
import com.accenture.meetspace.user.profile.application.usecases.command.changerole.ChangeUserRoleCommandHandler;
import com.accenture.meetspace.user.profile.application.usecases.command.changestatus.ChangeUserStatusCommand;
import com.accenture.meetspace.user.profile.application.usecases.command.changestatus.ChangeUserStatusCommandHandler;
import com.accenture.meetspace.user.profile.application.usecases.command.updateprofile.UpdateProfileCommand;
import com.accenture.meetspace.user.profile.application.usecases.command.updateprofile.UpdateProfileCommandHandler;
import com.accenture.meetspace.user.profile.application.usecases.query.user.ListUsersQuery;
import com.accenture.meetspace.user.profile.application.usecases.query.user.ListUsersQueryHandler;
import com.accenture.meetspace.user.profile.application.usecases.query.user.UserView;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;
import com.accenture.meetspace.user.units.stubs.InMemoryIdentityProvider;
import com.accenture.meetspace.user.units.stubs.InMemoryReservationCanceller;
import com.accenture.meetspace.user.units.stubs.InMemorySiteCatalog;
import com.accenture.meetspace.user.units.stubs.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("List users")
class ListUsersTest {

    @Nested
    @DisplayName("query")
    class Query {

        @Test
        @DisplayName("uses the default pagination when none is given")
        void usesTheDefaultPaginationWhenNoneIsGiven() {
            assertThat(new ListUsersQuery(null, null, null, null, null, null).pagination())
                    .isEqualTo(Pagination.DEFAULT);
        }
    }

    @Nested
    @DisplayName("handler")
    class Handler {

        private final InMemoryUserRepository users = new InMemoryUserRepository();
        private final InMemoryIdentityProvider identityProvider = new InMemoryIdentityProvider();
        private final ListUsersQueryHandler handler = new ListUsersQueryHandler(users);

        private UUID jsmith;
        private UUID akumar;

        private UUID registered(String username) {
            return new RegisterEmployeeCommandHandler(users, identityProvider).handle(new RegisterEmployeeCommand(
                    UUID.randomUUID(), username, username + "@accenture.com", "Passw0rd!", "John", "Smith")).userId();
        }

        @BeforeEach
        void givenAnAdminAndADeactivatedEmployee() {
            jsmith = registered("jsmith");
            akumar = registered("akumar");
            new ChangeUserRoleCommandHandler(users, identityProvider)
                    .handle(new ChangeUserRoleCommand(jsmith, RoleEnum.ADMIN));
            new UpdateProfileCommandHandler(users, new InMemorySiteCatalog())
                    .handle(new UpdateProfileCommand(jsmith, null, null, "Engineering", null, null));
            new ChangeUserStatusCommandHandler(users, identityProvider, new InMemoryReservationCanceller())
                    .handle(new ChangeUserStatusCommand(jsmith, akumar, false));
        }

        private List<UUID> ids(ListUsersQuery query) {
            return handler.handle(query).content().stream().map(UserView::id).toList();
        }

        @Test
        @DisplayName("lists every user without filter")
        void listsEveryUserWithoutFilter() {
            assertThat(ids(new ListUsersQuery(null, null, null, null, null, null)))
                    .containsExactlyInAnyOrder(jsmith, akumar);
        }

        @Test
        @DisplayName("filters by username")
        void filtersByUsername() {
            assertThat(ids(new ListUsersQuery("akumar", null, null, null, null, null))).containsExactly(akumar);
        }

        @Test
        @DisplayName("filters by email")
        void filtersByEmail() {
            assertThat(ids(new ListUsersQuery(null, "jsmith@accenture.com", null, null, null, null)))
                    .containsExactly(jsmith);
        }

        @Test
        @DisplayName("filters by department")
        void filtersByDepartment() {
            assertThat(ids(new ListUsersQuery(null, null, "Engineering", null, null, null))).containsExactly(jsmith);
        }

        @Test
        @DisplayName("filters by role")
        void filtersByRole() {
            assertThat(ids(new ListUsersQuery(null, null, null, RoleEnum.EMPLOYEE, null, null)))
                    .containsExactly(akumar);
        }

        @Test
        @DisplayName("filters by active status")
        void filtersByActiveStatus() {
            assertThat(ids(new ListUsersQuery(null, null, null, null, false, null))).containsExactly(akumar);
        }

        @Test
        @DisplayName("returns the requested page with its metadata")
        void returnsTheRequestedPageWithItsMetadata() {
            var page = handler.handle(new ListUsersQuery(null, null, null, null, null,
                    new Pagination(1, 1, List.of())));

            assertThat(page.content()).hasSize(1);
            assertThat(page.page().number()).isEqualTo(1);
            assertThat(page.page().size()).isEqualTo(1);
            assertThat(page.page().totalElements()).isEqualTo(2);
            assertThat(page.page().totalPages()).isEqualTo(2);
        }
    }
}
