package com.accenture.meetspace.user.units.stubs;

import com.accenture.meetspace.bootstrap.application.query.PageResponse;
import com.accenture.meetspace.bootstrap.application.query.Pagination;
import com.accenture.meetspace.user.profile.application.port.out.reader.UserReader;
import com.accenture.meetspace.user.profile.application.port.out.repository.UserRepository;
import com.accenture.meetspace.user.profile.application.usecases.query.directory.DirectoryEntry;
import com.accenture.meetspace.user.profile.application.usecases.query.user.ListUsersQuery;
import com.accenture.meetspace.user.profile.application.usecases.query.directory.SearchDirectoryQuery;
import com.accenture.meetspace.user.profile.application.usecases.query.user.UserView;
import com.accenture.meetspace.user.profile.domain.entities.Admin;
import com.accenture.meetspace.user.profile.domain.entities.Employee;
import com.accenture.meetspace.user.profile.domain.entities.User;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class InMemoryUserRepository implements UserRepository, UserReader {

    private record Row(User.Snapshot user, RoleEnum role) {
    }

    private final Map<UUID, Row> rows = new LinkedHashMap<>();
    private RuntimeException saveFailure;

    @Override
    public void save(Employee employee) {
        failIfRequested();
        var snapshot = employee.snapshot();
        rows.put(snapshot.user().id(), new Row(snapshot.user(), snapshot.role()));
    }

    @Override
    public void save(Admin admin) {
        failIfRequested();
        var snapshot = admin.snapshot();
        rows.put(snapshot.user().id(), new Row(snapshot.user(), snapshot.role()));
    }

    @Override
    public void save(User user) {
        failIfRequested();
        var snapshot = user.snapshot();
        rows.put(snapshot.id(), new Row(snapshot, rows.get(snapshot.id()).role()));
    }

    @Override
    public Optional<User> findUser(UUID id) {
        return Optional.ofNullable(rows.get(id)).map(row -> User.restore(row.user()));
    }

    @Override
    public Optional<Employee> findEmployee(UUID id) {
        return Optional.ofNullable(rows.get(id))
                .filter(row -> row.role() == RoleEnum.EMPLOYEE)
                .map(row -> Employee.restore(new Employee.Snapshot(row.user(), row.role())));
    }

    @Override
    public Optional<Admin> findAdmin(UUID id) {
        return Optional.ofNullable(rows.get(id))
                .filter(row -> row.role() == RoleEnum.ADMIN)
                .map(row -> Admin.restore(new Admin.Snapshot(row.user(), row.role())));
    }

    @Override
    public int countActiveAdmins() {
        return (int) rows.values().stream()
                .filter(row -> row.role() == RoleEnum.ADMIN && row.user().active())
                .count();
    }

    @Override
    public boolean existsByUsername(String username) {
        return rows.values().stream().anyMatch(row -> row.user().username().equals(username));
    }

    @Override
    public boolean existsByEmail(String email) {
        return rows.values().stream().anyMatch(row -> row.user().email().equals(email));
    }

    @Override
    public Optional<UserView> findById(UUID id) {
        return Optional.ofNullable(rows.get(id)).map(InMemoryUserRepository::view);
    }

    @Override
    public PageResponse<UserView> search(ListUsersQuery query) {
        return page(rows.values().stream()
                .filter(row -> contains(row.user().username(), query.username()))
                .filter(row -> contains(row.user().email(), query.email()))
                .filter(row -> contains(row.user().department(), query.department()))
                .filter(row -> query.role() == null || row.role() == query.role())
                .filter(row -> query.active() == null || row.user().active() == query.active())
                .map(InMemoryUserRepository::view)
                .toList(), query.pagination());
    }

    @Override
    public PageResponse<DirectoryEntry> searchDirectory(SearchDirectoryQuery query) {
        return page(rows.values().stream()
                .filter(row -> row.user().active())
                .filter(row -> contains(row.user().firstName(), query.name())
                        || contains(row.user().lastName(), query.name()))
                .map(row -> new DirectoryEntry(row.user().id(), row.user().username(), row.user().firstName(),
                        row.user().lastName()))
                .toList(), query.pagination());
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return rows.values().stream()
                .filter(row -> row.user().username().equals(username))
                .findFirst()
                .map(row -> User.restore(row.user()));
    }

    public void failOnSaveWith(RuntimeException failure) {
        saveFailure = failure;
    }

    public int count() {
        return rows.size();
    }

    private void failIfRequested() {
        if (saveFailure != null) {
            throw saveFailure;
        }
    }

    private static UserView view(Row row) {
        var user = row.user();
        return new UserView(user.id(), user.username(), user.email(), user.firstName(), user.lastName(),
                user.department(), user.phone(), user.defaultSiteId(), row.role(), user.active());
    }

    private static boolean contains(String value, String searched) {
        return searched == null || value != null
                && value.toLowerCase(Locale.ROOT).contains(searched.toLowerCase(Locale.ROOT));
    }

    private static <T> PageResponse<T> page(List<T> all, Pagination pagination) {
        var content = all.stream().skip(pagination.offset()).limit(pagination.size()).toList();
        return PageResponse.of(content, pagination, all.size());
    }
}
