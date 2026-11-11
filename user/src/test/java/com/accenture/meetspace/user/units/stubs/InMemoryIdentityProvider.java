package com.accenture.meetspace.user.units.stubs;

import com.accenture.meetspace.user.profile.application.port.out.identity.AuthTokens;
import com.accenture.meetspace.user.profile.application.port.out.identity.IdentityProvider;
import com.accenture.meetspace.user.profile.application.port.out.identity.IdentityUser;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class InMemoryIdentityProvider implements IdentityProvider {

    private final Map<String, AuthTokens> tokensByCredentials = new HashMap<>();
    private final List<String> revokedTokens = new ArrayList<>();
    private int exchanges;

    public record CreatedUser(UUID keycloakUserId, String username, String email, String password, String firstName,
                              String lastName, RoleEnum role) {
    }

    public record RoleChange(UUID keycloakUserId, RoleEnum role) {
    }

    private final List<CreatedUser> createdUsers = new ArrayList<>();
    private final Map<String, IdentityUser> accounts = new HashMap<>();
    private final List<UUID> deletedUsers = new ArrayList<>();
    private final List<RoleChange> roleChanges = new ArrayList<>();
    private final List<UUID> enabledUsers = new ArrayList<>();
    private final List<UUID> disabledUsers = new ArrayList<>();

    @Override
    public UUID createUser(String username, String email, String password, String firstName, String lastName,
                           RoleEnum role) {
        var keycloakUserId = UUID.randomUUID();
        createdUsers.add(new CreatedUser(keycloakUserId, username, email, password, firstName, lastName, role));
        accounts.put(username, new IdentityUser(keycloakUserId, username, email, firstName, lastName));
        return keycloakUserId;
    }

    @Override
    public Optional<IdentityUser> findUser(String username) {
        return Optional.ofNullable(accounts.get(username));
    }

    public UUID addExisting(String username, String email, String firstName, String lastName) {
        var keycloakUserId = UUID.randomUUID();
        accounts.put(username, new IdentityUser(keycloakUserId, username, email, firstName, lastName));
        return keycloakUserId;
    }

    @Override
    public void deleteUser(UUID keycloakUserId) {
        deletedUsers.add(keycloakUserId);
    }

    @Override
    public void changeRole(UUID keycloakUserId, RoleEnum role) {
        roleChanges.add(new RoleChange(keycloakUserId, role));
    }

    @Override
    public Optional<AuthTokens> exchangeCredentials(String username, String password) {
        exchanges++;
        return Optional.ofNullable(tokensByCredentials.get(username + ":" + password));
    }

    @Override
    public void revokeRefreshToken(String refreshToken) {
        revokedTokens.add(refreshToken);
    }

    public void accept(String username, String password, AuthTokens tokens) {
        tokensByCredentials.put(username + ":" + password, tokens);
    }

    public int exchanges() {
        return exchanges;
    }

    public List<String> revokedTokens() {
        return List.copyOf(revokedTokens);
    }

    public List<CreatedUser> createdUsers() {
        return List.copyOf(createdUsers);
    }

    public List<UUID> deletedUsers() {
        return List.copyOf(deletedUsers);
    }

    public List<RoleChange> roleChanges() {
        return List.copyOf(roleChanges);
    }

    @Override
    public void enableUser(UUID keycloakUserId) {
        enabledUsers.add(keycloakUserId);
    }

    @Override
    public void disableUser(UUID keycloakUserId) {
        disabledUsers.add(keycloakUserId);
    }

    public List<UUID> enabledUsers() {
        return List.copyOf(enabledUsers);
    }

    public List<UUID> disabledUsers() {
        return List.copyOf(disabledUsers);
    }
}
