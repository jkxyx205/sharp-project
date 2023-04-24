create table sys_report
(
    id bigint not null comment '主键'
        primary key,
    pageable bit null,
    sidx varchar(32) null,
    sord varchar(16) null,
    name varchar(32) null,
    query_sql text not null,
    summary bit default b'0' null,
    report_column_list text null,
    query_field_list text null,
    create_id bigint null,
    create_time datetime null,
    update_id bigint null,
    update_time datetime null,
    is_deleted bit null
)
    comment '报表';