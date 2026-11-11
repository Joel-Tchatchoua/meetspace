package com.accenture.meetspace.user.profile.infrastructure.adapters.out.persistence.repositories;

import com.accenture.meetspace.user.profile.infrastructure.adapters.out.persistence.models.UserModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataUserRepository extends JpaRepository<UserModel, String> {

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select u from UserModel u where u.id = :id")
    Optional<UserModel> findForUpdateById(@Param("id") String id);

    @Query(value = """
            select id
              from users
             where role = 'ADMIN'
               and active = true
               and deleted_at is null
               for update
            """, nativeQuery = true)
    List<String> lockActiveAdminIds();

    Optional<UserModel> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
