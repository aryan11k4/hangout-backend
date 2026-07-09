create table users (
    id              uuid primary key,
    username        varchar(50) not null,
    email           varchar(255) not null,
    password        varchar(255) not null,
    display_name    varchar(100),
    profile_picture varchar(500),
    bio             varchar(500),
    role            varchar(20) not null,
    is_enabled      boolean not null default true,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now(),
    constraint uk_users_username unique (username),
    constraint uk_users_email unique (email)
);

create table groups (
    id          uuid primary key,
    name        varchar(100) not null,
    visibility  varchar(20) not null,
    invite_code varchar(20),
    owner_id    uuid not null references users (id),
    created_at  timestamptz not null default now(),
    constraint uk_groups_invite_code unique (invite_code)
);

create table group_members (
    id        bigint generated always as identity primary key,
    group_id  uuid not null references groups (id) on delete cascade,
    user_id   uuid not null references users (id) on delete cascade,
    role      varchar(20) not null,
    joined_at timestamptz not null default now(),
    constraint uk_group_members_group_user unique (group_id, user_id)
);

create table join_requests (
    id           uuid primary key,
    group_id     uuid not null references groups (id) on delete cascade,
    user_id      uuid not null references users (id) on delete cascade,
    status       varchar(20) not null,
    requested_at timestamptz not null default now(),
    constraint uk_join_requests_group_user unique (group_id, user_id)
);

create index idx_group_members_group_id on group_members (group_id);
create index idx_group_members_user_id on group_members (user_id);
create index idx_join_requests_group_id_status on join_requests (group_id, status);
create index idx_groups_owner_id on groups (owner_id);
