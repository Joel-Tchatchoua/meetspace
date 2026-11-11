package com.accenture.meetspace.user.profile.infrastructure.adapters.out.persistence.mappers;

import com.accenture.meetspace.user.profile.domain.entities.User;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;
import com.accenture.meetspace.user.profile.infrastructure.adapters.out.persistence.models.UserModel;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserModelMapper {

    public UserModel toNewModel(User.Snapshot snapshot, RoleEnum role) {
        var model = new UserModel();
        model.setId(snapshot.id().toString());
        model.setKeycloakUserId(snapshot.keycloakUserId().toString());
        model.setUsername(snapshot.username());
        model.setEmail(snapshot.email());
        update(model, snapshot, role);
        return model;
    }

    public void update(UserModel model, User.Snapshot snapshot, RoleEnum role) {
        model.setFirstName(snapshot.firstName());
        model.setLastName(snapshot.lastName());
        model.setDepartment(snapshot.department());
        model.setPhone(snapshot.phone());
        model.setDefaultSiteId(snapshot.defaultSiteId() == null ? null : snapshot.defaultSiteId().toString());
        model.setRole(role);
        model.setActive(snapshot.active());
    }

    public User.Snapshot toSnapshot(UserModel model) {
        return new User.Snapshot(
                UUID.fromString(model.getId()),
                UUID.fromString(model.getKeycloakUserId()),
                model.getUsername(),
                model.getEmail(),
                model.getFirstName(),
                model.getLastName(),
                model.getDepartment(),
                model.getPhone(),
                model.getDefaultSiteId() == null ? null : UUID.fromString(model.getDefaultSiteId()),
                model.isActive());
    }
}
