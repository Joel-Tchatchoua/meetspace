package com.accenture.meetspace.user.integration;

import com.accenture.meetspace.bootstrap.test.integration.JpaIntegrationTest;
import com.accenture.meetspace.user.profile.domain.entities.Admin;
import com.accenture.meetspace.user.profile.domain.entities.Employee;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;
import com.accenture.meetspace.user.profile.domain.exceptions.LastActiveAdminException;
import com.accenture.meetspace.user.profile.infrastructure.adapters.out.persistence.mappers.UserModelMapper;
import com.accenture.meetspace.user.profile.infrastructure.adapters.out.persistence.repositories.JpaUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JpaIntegrationTest
@Import({JpaUserRepository.class, UserModelMapper.class})
@DisplayName("JpaUserRepository")
class JpaUserRepositoryTest {

    @Autowired
    JpaUserRepository repository;

    @Autowired
    PlatformTransactionManager transactionManager;

    private static Employee employee(String username) {
        return Employee.create(UUID.randomUUID(), UUID.randomUUID(), username, username + "@accenture.com", "John",
                "Smith");
    }

    private static Admin admin(String username) {
        return Admin.create(UUID.randomUUID(), UUID.randomUUID(), username, username + "@accenture.com", "John",
                "Smith");
    }

    @Nested
    @DisplayName("saves and reloads")
    class SavesAndReloads {

        @Test
        @DisplayName("an employee with every field")
        void anEmployeeWithEveryField() {
            var employee = employee("jsmith");
            var id = employee.snapshot().user().id();
            repository.save(employee);
            var user = repository.findUser(id).orElseThrow();
            var siteId = UUID.randomUUID();
            user.changeProfile("Johnny", "Smithson", "Engineering", "+230 5555 0000", siteId);
            repository.save(user);

            var reloaded = repository.findEmployee(id).orElseThrow().snapshot();

            assertThat(reloaded.role()).isEqualTo(RoleEnum.EMPLOYEE);
            assertThat(reloaded.user().keycloakUserId()).isEqualTo(employee.snapshot().user().keycloakUserId());
            assertThat(reloaded.user().username()).isEqualTo("jsmith");
            assertThat(reloaded.user().email()).isEqualTo("jsmith@accenture.com");
            assertThat(reloaded.user().firstName()).isEqualTo("Johnny");
            assertThat(reloaded.user().lastName()).isEqualTo("Smithson");
            assertThat(reloaded.user().department()).isEqualTo("Engineering");
            assertThat(reloaded.user().phone()).isEqualTo("+230 5555 0000");
            assertThat(reloaded.user().defaultSiteId()).isEqualTo(siteId);
            assertThat(reloaded.user().active()).isTrue();
        }

        @Test
        @DisplayName("an admin")
        void anAdmin() {
            var admin = admin("root");
            var id = admin.snapshot().user().id();

            repository.save(admin);

            assertThat(repository.findAdmin(id).orElseThrow().snapshot().role()).isEqualTo(RoleEnum.ADMIN);
            assertThat(repository.findEmployee(id)).isEmpty();
        }

        @Test
        @DisplayName("nothing for an unknown user")
        void nothingForAnUnknownUser() {
            var id = UUID.randomUUID();

            assertThat(repository.findUser(id)).isEmpty();
            assertThat(repository.findEmployee(id)).isEmpty();
            assertThat(repository.findAdmin(id)).isEmpty();
        }
    }

    @Nested
    @DisplayName("finds by username")
    class FindsByUsername {

        @Test
        @DisplayName("a user, employee or admin")
        void aUser() {
            var employee = employee("jsmith");
            repository.save(employee);
            repository.save(admin("root"));

            assertThat(repository.findByUsername("jsmith").orElseThrow().snapshot().id())
                    .isEqualTo(employee.snapshot().user().id());
            assertThat(repository.findByUsername("root")).isPresent();
        }

        @Test
        @DisplayName("nothing for an unknown username")
        void nothingForAnUnknownUsername() {
            assertThat(repository.findByUsername("ghost")).isEmpty();
        }
    }

    @Nested
    @DisplayName("changes the role")
    class ChangesTheRole {

        @Test
        @DisplayName("on the same row when an employee is promoted")
        void onTheSameRowWhenAnEmployeeIsPromoted() {
            var employee = employee("jsmith");
            var id = employee.snapshot().user().id();
            repository.save(employee);

            repository.save(repository.findEmployee(id).orElseThrow().promote());

            assertThat(repository.findAdmin(id)).isPresent();
            assertThat(repository.findEmployee(id)).isEmpty();
        }

        @Test
        @DisplayName("without changing it when only the user is saved")
        void withoutChangingItWhenOnlyTheUserIsSaved() {
            var admin = admin("root");
            var id = admin.snapshot().user().id();
            repository.save(admin);
            var user = repository.findUser(id).orElseThrow();
            user.changeProfile(null, null, "Engineering", null, null);

            repository.save(user);

            assertThat(repository.findAdmin(id).orElseThrow().snapshot().user().department())
                    .isEqualTo("Engineering");
        }
    }

    @Nested
    @DisplayName("counts active admins")
    class CountsActiveAdmins {

        @Test
        @DisplayName("leaving out employees and inactive admins")
        void leavingOutEmployeesAndInactiveAdmins() {
            repository.save(employee("jsmith"));
            repository.save(admin("root"));
            var inactive = admin("former");
            repository.save(inactive);
            var user = repository.findUser(inactive.snapshot().user().id()).orElseThrow();
            user.deactivate(UUID.randomUUID());
            repository.save(user);

            assertThat(repository.countActiveAdmins()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("knows who exists")
    class KnowsWhoExists {

        @Test
        @DisplayName("by username, for employees and admins")
        void byUsername() {
            repository.save(employee("jsmith"));
            repository.save(admin("root"));

            assertThat(repository.existsByUsername("jsmith")).isTrue();
            assertThat(repository.existsByUsername("root")).isTrue();
            assertThat(repository.existsByUsername("ghost")).isFalse();
        }

        @Test
        @DisplayName("by email, for employees and admins")
        void byEmail() {
            repository.save(employee("jsmith"));
            repository.save(admin("root"));

            assertThat(repository.existsByEmail("jsmith@accenture.com")).isTrue();
            assertThat(repository.existsByEmail("root@accenture.com")).isTrue();
            assertThat(repository.existsByEmail("ghost@accenture.com")).isFalse();
        }
    }

    @Nested
    @DisplayName("fails immediately on save, so that the Keycloak user can be deleted")
    class FailsImmediatelyOnSave {

        @Test
        @DisplayName("for a username already taken")
        void forAUsernameAlreadyTaken() {
            repository.save(employee("jsmith"));
            var sameUsername = Employee.create(UUID.randomUUID(), UUID.randomUUID(), "jsmith", "other@accenture.com",
                    "Jane", "Smith");

            assertThatThrownBy(() -> repository.save(sameUsername))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("for an email already taken")
        void forAnEmailAlreadyTaken() {
            repository.save(employee("jsmith"));
            var sameEmail = Employee.create(UUID.randomUUID(), UUID.randomUUID(), "jane", "jsmith@accenture.com",
                    "Jane", "Smith");

            assertThatThrownBy(() -> repository.save(sameEmail))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("for a Keycloak user already linked")
        void forAKeycloakUserAlreadyLinked() {
            var keycloakUserId = UUID.randomUUID();
            repository.save(Employee.create(UUID.randomUUID(), keycloakUserId, "jsmith", "jsmith@accenture.com",
                    "John", "Smith"));
            var sameKeycloakUser = Employee.create(UUID.randomUUID(), keycloakUserId, "jane", "jane@accenture.com",
                    "Jane", "Smith");

            assertThatThrownBy(() -> repository.save(sameKeycloakUser))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("under concurrency")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    class UnderConcurrency {

        private TransactionTemplate inTransaction() {
            return new TransactionTemplate(transactionManager);
        }

        @Test
        @DisplayName("rejects a conflicting update of the same user")
        void rejectsAConflictingUpdateOfTheSameUser() throws Exception {
            var employee = employee("jsmith");
            var id = employee.snapshot().user().id();
            inTransaction().executeWithoutResult(status -> repository.save(employee));
            var barrier = new CyclicBarrier(2);

            var failures = concurrently(() -> inTransaction().executeWithoutResult(status -> {
                var user = repository.findUser(id).orElseThrow();
                await(barrier);
                user.changeProfile(null, null, "Engineering", null, null);
                repository.save(user);
            }));

            assertThat(failures).singleElement().isInstanceOf(ObjectOptimisticLockingFailureException.class);
        }

        @Test
        @DisplayName("never demotes the two last active admins at the same time")
        void neverDemotesTheTwoLastActiveAdminsAtTheSameTime() throws Exception {
            var first = admin("first");
            var second = admin("second");
            inTransaction().executeWithoutResult(status -> {
                repository.save(first);
                repository.save(second);
            });
            var barrier = new CyclicBarrier(2);
            var ids = new ConcurrentLinkedQueue<>(
                    List.of(first.snapshot().user().id(), second.snapshot().user().id()));

            var failures = concurrently(() -> inTransaction().executeWithoutResult(status -> {
                var admin = repository.findAdmin(ids.remove()).orElseThrow();
                await(barrier);
                repository.save(admin.demote(repository.countActiveAdmins()));
            }));

            assertThat(failures).singleElement().isInstanceOf(LastActiveAdminException.class);
            Integer remainingAdmins = inTransaction().execute(status -> repository.countActiveAdmins());
            assertThat(remainingAdmins).isEqualTo(1);
        }

        private static void await(CyclicBarrier barrier) {
            try {
                barrier.await(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        private static List<Throwable> concurrently(Runnable task) throws Exception {
            try (var executor = Executors.newFixedThreadPool(2)) {
                var futures = new ArrayList<Future<?>>();
                futures.add(executor.submit(task));
                futures.add(executor.submit(task));
                var failures = new ArrayList<Throwable>();
                for (var future : futures) {
                    try {
                        future.get(60, TimeUnit.SECONDS);
                    } catch (ExecutionException e) {
                        failures.add(e.getCause());
                    }
                }
                return failures;
            }
        }
    }
}
