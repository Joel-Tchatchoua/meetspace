package com.accenture.meetspace.user.profile.domain.entities;

import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;

import java.util.UUID;

public final class Employee {

    public record Snapshot(User.Snapshot user, RoleEnum role) {
    }

    private final User user;
    private final RoleEnum role;

    private Employee(User user) {
        this.user = user;
        this.role = RoleEnum.EMPLOYEE;
    }

    public static Employee create(UUID id, UUID keycloakUserId, String username, String email, String firstName,
                                  String lastName) {
        return new Employee(User.create(id, keycloakUserId, username, email, firstName, lastName));
    }

    public static Employee restore(Snapshot snapshot) {
        return new Employee(User.restore(snapshot.user()));
    }

    static Employee from(User user) {
        return new Employee(user);
    }

    public Admin promote() {
        return Admin.from(user);
    }

    public Snapshot snapshot() {
        return new Snapshot(user.snapshot(), role);
    }
}
