package com.accenture.meetspace.user.profile.application.port.out.repository;

import com.accenture.meetspace.user.profile.domain.entities.Admin;
import com.accenture.meetspace.user.profile.domain.entities.Employee;
import com.accenture.meetspace.user.profile.domain.entities.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    void save(Employee employee);

    void save(Admin admin);

    void save(User user);

    Optional<User> findUser(UUID id);

    Optional<Employee> findEmployee(UUID id);

    Optional<Admin> findAdmin(UUID id);

    int countActiveAdmins();

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
