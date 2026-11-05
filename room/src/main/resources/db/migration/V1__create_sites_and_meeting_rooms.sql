create table sites
(
    id         char(36)     not null,
    code       varchar(50)  not null,
    name       varchar(100) not null,
    address    varchar(255) not null,
    time_zone  varchar(64)  not null,
    version    bigint       not null,
    created_at datetime(6)  not null,
    updated_at datetime(6)  not null,
    deleted_at datetime(6),
    constraint pk_sites primary key (id)
);

create table meeting_rooms
(
    id         char(36)     not null,
    site_id    char(36)     not null,
    code       varchar(50)  not null,
    name       varchar(100) not null,
    floor      int          not null,
    capacity   int          not null,
    status     varchar(20)  not null,
    version    bigint       not null,
    created_at datetime(6)  not null,
    updated_at datetime(6)  not null,
    deleted_at datetime(6),
    constraint pk_meeting_rooms primary key (id),
    constraint fk_meeting_rooms_site foreign key (site_id) references sites (id),
    constraint ck_meeting_rooms_capacity check (capacity between 1 and 200),
    constraint ck_meeting_rooms_status check (status in ('ACTIVE', 'MAINTENANCE', 'CLOSED'))
);

create table meeting_room_amenities
(
    room_id char(36)    not null,
    amenity varchar(30) not null,
    constraint pk_meeting_room_amenities primary key (room_id, amenity),
    constraint fk_meeting_room_amenities_room foreign key (room_id) references meeting_rooms (id)
);

create unique index uk_sites_active_code on sites ((case when deleted_at is null then code end));

create unique index uk_sites_active_name on sites ((case when deleted_at is null then name end));

create unique index uk_meeting_rooms_active_site_code on meeting_rooms (
    (case when deleted_at is null then site_id end),
    (case when deleted_at is null then code end)
);
