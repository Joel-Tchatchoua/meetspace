create table users
(
    id               char(36)     not null,
    keycloak_user_id char(36)     not null,
    username         varchar(100) not null,
    email            varchar(255) not null,
    first_name       varchar(100) not null,
    last_name        varchar(100) not null,
    department       varchar(100),
    phone            varchar(30),
    default_site_id  char(36),
    role             varchar(20)  not null,
    active           boolean      not null,
    version          bigint       not null,
    created_at       datetime(6)  not null,
    updated_at       datetime(6)  not null,
    deleted_at       datetime(6),
    constraint pk_users primary key (id),
    constraint ck_users_role check (role in ('ADMIN', 'EMPLOYEE'))
);

create unique index uk_users_active_keycloak_user_id on users ((case when deleted_at is null then keycloak_user_id end));

create unique index uk_users_active_username on users ((case when deleted_at is null then username end));

create unique index uk_users_active_email on users ((case when deleted_at is null then email end));
