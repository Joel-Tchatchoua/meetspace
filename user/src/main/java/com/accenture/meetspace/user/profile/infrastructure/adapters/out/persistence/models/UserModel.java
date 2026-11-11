package com.accenture.meetspace.user.profile.infrastructure.adapters.out.persistence.models;

import com.accenture.meetspace.bootstrap.infrastructure.persistence.VersionModel;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserModel extends VersionModel {

    @Column(nullable = false, length = 36)
    private String keycloakUserId;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(length = 100)
    private String department;

    @Column(length = 30)
    private String phone;

    @Column(length = 36)
    private String defaultSiteId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private RoleEnum role;

    @Column(nullable = false)
    private boolean active;
}
