package com.accenture.meetspace.user.profile.domain.entities;

import com.accenture.meetspace.user.profile.domain.exceptions.CannotDeactivateOwnAccountException;

import java.util.UUID;

public final class User {

    public record Snapshot(UUID id, UUID keycloakUserId, String username, String email, String firstName,
                           String lastName, String department, String phone, UUID defaultSiteId, boolean active) {
    }

    private final UUID id;
    private final UUID keycloakUserId;
    private final String username;
    private final String email;
    private String firstName;
    private String lastName;
    private String department;
    private String phone;
    private UUID defaultSiteId;
    private boolean active;

    private User(UUID id, UUID keycloakUserId, String username, String email, String firstName, String lastName) {
        this.id = id;
        this.keycloakUserId = keycloakUserId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = true;
    }

    static User create(UUID id, UUID keycloakUserId, String username, String email, String firstName,
                       String lastName) {
        return new User(id, keycloakUserId, username, email, firstName, lastName);
    }

    public static User restore(Snapshot snapshot) {
        var user = new User(snapshot.id(), snapshot.keycloakUserId(), snapshot.username(), snapshot.email(),
                snapshot.firstName(), snapshot.lastName());
        user.department = snapshot.department();
        user.phone = snapshot.phone();
        user.defaultSiteId = snapshot.defaultSiteId();
        user.active = snapshot.active();
        return user;
    }

    public void changeProfile(String firstName, String lastName, String department, String phone,
                              UUID defaultSiteId) {
        this.firstName = firstName != null ? firstName : this.firstName;
        this.lastName = lastName != null ? lastName : this.lastName;
        this.department = department != null ? department : this.department;
        this.phone = phone != null ? phone : this.phone;
        this.defaultSiteId = defaultSiteId != null ? defaultSiteId : this.defaultSiteId;
    }

    public boolean activate() {
        if (active) {
            return false;
        }
        active = true;
        return true;
    }

    public boolean deactivate(UUID actorId) {
        if (id.equals(actorId)) {
            throw new CannotDeactivateOwnAccountException();
        }
        if (!active) {
            return false;
        }
        active = false;
        return true;
    }

    public boolean isActive() {
        return active;
    }

    public Snapshot snapshot() {
        return new Snapshot(id, keycloakUserId, username, email, firstName, lastName, department, phone,
                defaultSiteId, active);
    }
}
