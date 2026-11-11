package com.accenture.meetspace.user.units;

import com.accenture.meetspace.bootstrap.application.query.InvalidQueryException;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommand;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommandHandler;
import com.accenture.meetspace.user.profile.application.usecases.command.changestatus.ChangeUserStatusCommand;
import com.accenture.meetspace.user.profile.application.usecases.command.changestatus.ChangeUserStatusCommandHandler;
import com.accenture.meetspace.user.profile.application.usecases.query.directory.DirectoryEntry;
import com.accenture.meetspace.user.profile.application.usecases.query.directory.SearchDirectoryQuery;
import com.accenture.meetspace.user.profile.application.usecases.query.directory.SearchDirectoryQueryHandler;
import com.accenture.meetspace.user.units.stubs.InMemoryIdentityProvider;
import com.accenture.meetspace.user.units.stubs.InMemoryReservationCanceller;
import com.accenture.meetspace.user.units.stubs.InMemoryUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Search directory")
class SearchDirectoryTest {

    @Nested
    @DisplayName("query")
    class Query {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = "  ")
        @DisplayName("rejects a missing name")
        void rejectsAMissingName(String name) {
            assertThatThrownBy(() -> new SearchDirectoryQuery(name, null))
                    .isInstanceOf(InvalidQueryException.class)
                    .hasFieldOrPropertyWithValue("field", "name");
        }
    }

    @Nested
    @DisplayName("handler")
    class Handler {

        private final InMemoryUserRepository users = new InMemoryUserRepository();
        private final InMemoryIdentityProvider identityProvider = new InMemoryIdentityProvider();
        private final SearchDirectoryQueryHandler handler = new SearchDirectoryQueryHandler(users);

        private UUID registered(String username, String firstName, String lastName) {
            return new RegisterEmployeeCommandHandler(users, identityProvider).handle(new RegisterEmployeeCommand(
                    UUID.randomUUID(), username, username + "@accenture.com", "Passw0rd!", firstName, lastName))
                    .userId();
        }

        private List<String> usernames(String name) {
            return handler.handle(new SearchDirectoryQuery(name, null)).content().stream()
                    .map(DirectoryEntry::username)
                    .toList();
        }

        @Test
        @DisplayName("finds colleagues by first or last name")
        void findsColleaguesByFirstOrLastName() {
            registered("jsmith", "John", "Smith");
            registered("akumar", "Asha", "Kumar");
            registered("jdoe", "Jane", "Smithson");

            assertThat(usernames("asha")).containsExactly("akumar");
            assertThat(usernames("smith")).containsExactlyInAnyOrder("jsmith", "jdoe");
        }

        @Test
        @DisplayName("leaves deactivated users out")
        void leavesDeactivatedUsersOut() {
            var userId = registered("jsmith", "John", "Smith");
            new ChangeUserStatusCommandHandler(users, identityProvider, new InMemoryReservationCanceller())
                    .handle(new ChangeUserStatusCommand(UUID.randomUUID(), userId, false));

            assertThat(usernames("John")).isEmpty();
        }

        @Test
        @DisplayName("returns only basic public fields")
        void returnsOnlyBasicPublicFields() {
            var userId = registered("jsmith", "John", "Smith");

            var entry = handler.handle(new SearchDirectoryQuery("John", null)).content().getFirst();

            assertThat(entry).isEqualTo(new DirectoryEntry(userId, "jsmith", "John", "Smith"));
            assertThat(Arrays.stream(DirectoryEntry.class.getRecordComponents()).map(component -> component.getName()))
                    .containsExactly("userId", "username", "firstName", "lastName");
        }
    }
}
