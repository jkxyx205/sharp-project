create table sys_dict
(
    type varchar(32) not null,
    name varchar(32) not null,
    label varchar(32) not null,
    sort int null,
    primary key (type, name)
)
    charset=utf8mb4;

create table sys_property
(
    name varchar(32) not null
        primary key,
    value varchar(255) not null
)
    charset=utf8mb4;

