package com.accenture.meetspace.user.profile.domain.entities;

import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;
import com.accenture.meetspace.user.profile.domain.exceptions.LastActiveAdminException;

import java.util.UUID;

public final class Admin {

    public record Snapshot(User.Snapshot user, RoleEnum role) {
    }

    private final User user;
    private final RoleEnum role;

    private Admin(User user) {
        this.user = user;
        this.role = RoleEnum.ADMIN;
    }

    public static Admin create(UUID id, UUID keycloakUserId, String username, String email, String firstName,
                               String lastName) {
        return new Admin(User.create(id, keycloakUserId, username, email, firstName, lastName));
    }

    public static Admin restore(Snapshot snapshot) {
        return new Admin(User.restore(snapshot.user()));
    }

    static Admin from(User user) {
        return new Admin(user);
    }

    public Employee demote(int activeAdmins) {
        if (user.isActive() && activeAdmins <= 1) {
            throw new LastActiveAdminException();
        }
        return Employee.from(user);
    }

    public Snapshot snapshot() {
        return new Snapshot(user.snapshot(), role);
    }
}
