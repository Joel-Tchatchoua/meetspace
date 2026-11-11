package com.accenture.meetspace.user.profile.infrastructure.adapters.out.persistence.repositories;

import com.accenture.meetspace.user.profile.application.port.out.repository.UserRepository;
import com.accenture.meetspace.user.profile.domain.entities.Admin;
import com.accenture.meetspace.user.profile.domain.entities.Employee;
import com.accenture.meetspace.user.profile.domain.entities.User;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;
import com.accenture.meetspace.user.profile.infrastructure.adapters.out.persistence.mappers.UserModelMapper;
import com.accenture.meetspace.user.profile.infrastructure.adapters.out.persistence.models.UserModel;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository jpaRepository;
    private final UserModelMapper mapper;

    public JpaUserRepository(SpringDataUserRepository jpaRepository, UserModelMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(Employee employee) {
        var snapshot = employee.snapshot();
        save(snapshot.user(), snapshot.role());
    }

    @Override
    public void save(Admin admin) {
        var snapshot = admin.snapshot();
        save(snapshot.user(), snapshot.role());
    }

    @Override
    public void save(User user) {
        var snapshot = user.snapshot();
        var model = jpaRepository.findById(snapshot.id().toString()).orElseThrow();
        mapper.update(model, snapshot, model.getRole());
        jpaRepository.saveAndFlush(model);
    }

    @Override
    public Optional<User> findUser(UUID id) {
        return findModel(id).map(model -> User.restore(mapper.toSnapshot(model)));
    }

    @Override
    public Optional<Employee> findEmployee(UUID id) {
        return findModel(id)
                .filter(model -> model.getRole() == RoleEnum.EMPLOYEE)
                .map(model -> Employee.restore(new Employee.Snapshot(mapper.toSnapshot(model), model.getRole())));
    }

    @Override
    public Optional<Admin> findAdmin(UUID id) {
        return findModel(id)
                .filter(model -> model.getRole() == RoleEnum.ADMIN)
                .map(model -> Admin.restore(new Admin.Snapshot(mapper.toSnapshot(model), model.getRole())));
    }

    @Override
    public int countActiveAdmins() {
        return jpaRepository.lockActiveAdminIds().size();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(model -> User.restore(mapper.toSnapshot(model)));
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    private Optional<UserModel> findModel(UUID id) {
        return jpaRepository.findForUpdateById(id.toString());
    }

    private void save(User.Snapshot snapshot, RoleEnum role) {
        jpaRepository.findById(snapshot.id().toString()).ifPresentOrElse(
                model -> {
                    mapper.update(model, snapshot, role);
                    jpaRepository.saveAndFlush(model);
                },
                () -> jpaRepository.saveAndFlush(mapper.toNewModel(snapshot, role)));
    }
}
