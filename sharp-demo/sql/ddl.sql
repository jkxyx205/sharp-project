create table mm_serial_number
(
    id bigint not null comment '主键'
        primary key,
    code varchar(32) not null comment '外部可见，唯一code',
    status varchar(16) null,
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null
)
comment '序列号';

create table sys_dict
(
    type varchar(32) not null,
    name varchar(32) not null,
    label varchar(32) not null,
    sort int null,
    primary key (type, name)
)
charset=utf8mb4;

create table sys_document
(
    id bigint not null
        primary key,
    name varchar(255) not null,
    extension varchar(16) null,
    content_type varchar(32) null,
    size int null,
    group_name varchar(255) not null,
    path varchar(255) not null,
    created_at datetime not null
)
charset=utf8;

create table sys_form
(
    id bigint not null
        primary key,
    code varchar(32) null,
    name varchar(32) not null,
    form_advice_name varchar(32) null,
    table_name varchar(32) null,
    repository_name varchar(32) null,
    storage_strategy varchar(16) null,
    created_by bigint not null,
    created_at datetime null,
    updated_by bigint not null,
    updated_at datetime null,
    is_deleted bit not null
)
charset=utf8mb4;

create table sys_form_configurer
(
    id bigint not null
        primary key,
    name varchar(32) null,
    label varchar(16) not null,
    type varchar(32) not null,
    validators text null,
    options varchar(1024) null,
    default_value varchar(64) null,
    placeholder varchar(32) null,
    additional_info varchar(1024) null,
    created_by bigint not null,
    created_at datetime null,
    updated_by bigint not null,
    updated_at datetime null,
    is_deleted bit not null
)
charset=utf8mb4;

create table sys_form_cpn_configurer
(
    id bigint not null
        primary key,
    form_id bigint not null,
    config_id bigint not null,
    order_num int null,
    created_by bigint not null,
    created_at datetime null,
    updated_by bigint not null,
    updated_at datetime null,
    is_deleted bit not null
)
charset=utf8mb4;

create table sys_form_cpn_value
(
    id bigint not null
        primary key,
    value text null,
    form_cpn_id bigint not null,
    form_id bigint not null,
    config_id bigint not null,
    instance_id bigint not null,
    created_by bigint not null,
    created_at datetime null,
    updated_by bigint not null,
    updated_at datetime null,
    is_deleted bit not null
)
charset=utf8mb4;

create table sys_form_instance_first
(
    id bigint not null
        primary key,
    name varchar(32) null,
    age int null,
    hobby varchar(32) null,
    hobby2 varchar(32) null,
    agree varchar(32) null,
    remark varchar(32) null,
    sex varchar(32) null,
    mobile varchar(32) null,
    file varchar(4369) null,
    email varchar(32) null,
    date date null,
    info json null,
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null
)
comment '用户信息';

create table sys_property
(
    name varchar(32) not null
        primary key,
    value varchar(255) not null
)
charset=utf8mb4;

create table sys_report
(
    id bigint not null comment '主键'
        primary key,
    pageable bit null,
    sidx varchar(32) null,
    sord varchar(16) null,
    name varchar(32) null,
    query_sql text not null,
    summary_column_names varchar(128) null,
    report_column_list text null,
    query_field_list text null,
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null
)
comment '报表';

create table sys_user
(
    username varchar(255) null,
    name varchar(255) null,
    id int not null
        primary key
);

create table t_book
(
    id bigint not null comment '主键'
        primary key,
    title varchar(32) not null,
    person_id bigint null,
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null,
    constraint t_book_title_uindex
        unique (title)
)
comment '书';

create table t_book_tag
(
    book_id bigint not null,
    tag_id bigint not null,
    is_deleted bit default b'0' not null,
    constraint t_book_tag_pk
        unique (book_id, tag_id)
);

create table t_company
(
    id bigint not null comment '主键'
        primary key,
    name varchar(32) null,
    address varchar(32) null,
    phone varchar(32) null,
    first_name varchar(32) null,
    last_name varchar(32) null,
    contact_phone varchar(32) null,
    role_id bigint null
)
comment '公司';

create table t_company_vendor
(
    id bigint not null comment '主键'
        primary key,
    name varchar(32) null,
    company_id bigint null
)
comment '供应商';

create table t_demo
(
    id bigint not null comment '主键'
        primary key,
    title varchar(255) not null comment '任务名称',
    start_time timestamp null comment '开始时间',
    end_time timestamp null comment '结束时间',
    demo_type varchar(16) not null comment '任务类型',
    created_by bigint not null comment '创建人',
    created_at timestamp null comment '创建时间',
    updated_by bigint not null comment '更新人',
    updated_at timestamp null comment '更新时间',
    is_deleted bit not null comment '是否删除',
    version int null,
    work_time int default 0 null
)
comment '任务';

create table t_farmer
(
    id bigint not null comment '主键'
        primary key,
    name varchar(32) null,
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null
)
comment '农民';

create table t_film
(
    seq_id varchar(32) not null comment '主键'
        primary key,
    title varchar(32) null
)
comment '电影';

create table t_info
(
    id bigint not null comment '主键'
        primary key,
    name varchar(32) null,
    age int null,
    native_place varchar(32) null,
    hobby varchar(32) null,
    agree varchar(32) null,
    remark varchar(32) null,
    sex varchar(32) null,
    mobile varchar(32) null,
    file varchar(10000) null,
    email varchar(32) null,
    date date null,
    info json null,
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null
)
comment '用户信息';

create table t_material
(
    id varchar(32) not null comment '主键'
        primary key,
    name varchar(32) null,
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null
)
comment '农民';

create table t_message
(
    id bigint not null comment '主键'
        primary key,
    text varchar(32) null,
    created_at datetime null
)
comment '消息';

create table t_message2
(
    seq bigint not null comment '主键'
        primary key,
    text varchar(32) null
)
comment '消息2';

create table t_person_id_card
(
    id bigint not null comment '主键'
        primary key,
    id_num varchar(32) null,
    address varchar(32) null,
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null
);

create table t_person
(
    id bigint not null comment '主键'
        primary key,
    name varchar(32) null,
    id_card_id bigint null,
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null,
    sex char null,
    constraint t_person_t_person_id_card_id_fk
        foreign key (id_card_id) references t_person_id_card (id)
);

create table t_person_role
(
    person_id bigint not null,
    role_id bigint not null,
    is_deleted bit default b'0' not null,
    constraint t_person_role_pk
        unique (person_id, role_id)
);

create table t_project1
(
    id bigint not null
        primary key,
    title varchar(255) not null,
    description varchar(255) null,
    map text null,
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null,
    cover_url varchar(500) null,
    owner_id bigint null,
    sex char null comment '性别',
    address text null,
    status varchar(255) null,
    list text null,
    phone_number varchar(32) null
)
charset=utf8mb4;

create table t_project2
(
    id bigint not null
        primary key,
    title varchar(255) not null,
    description varchar(255) null,
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null,
    cover_url varchar(500) null,
    owner_id bigint null,
    sex char null comment '性别',
    address text null,
    status varchar(255) null,
    list text null,
    phone_number varchar(32) null
)
charset=utf8mb4;

create table t_project3
(
    id bigint not null
        primary key,
    title varchar(255) not null,
    description varchar(255) null,
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null,
    cover_url varchar(500) null,
    owner_id bigint null,
    sex char null comment '性别',
    address text null,
    status varchar(255) null,
    list text null,
    phone_number varchar(32) null
)
charset=utf8mb4;

create table t_project_detail1
(
    id bigint not null
        primary key,
    title varchar(32) not null,
    project_id bigint not null,
    group_id bigint not null,
    created_by bigint not null,
    created_at datetime null,
    updated_by bigint not null,
    updated_at datetime null,
    is_deleted bit not null
)
charset=utf8mb4;

create table t_project_detail2
(
    id bigint not null
        primary key,
    title varchar(32) not null,
    project_id bigint not null,
    group_id bigint not null,
    created_by bigint not null,
    created_at datetime null,
    updated_by bigint not null,
    updated_at datetime null,
    is_deleted bit not null
)
charset=utf8mb4;

create table t_project_detail3
(
    id bigint not null
        primary key,
    title varchar(32) not null,
    project_id bigint not null,
    group_id bigint not null,
    created_by bigint not null,
    created_at datetime null,
    updated_by bigint not null,
    updated_at datetime null,
    is_deleted bit not null
)
charset=utf8mb4;

create table t_role
(
    id bigint not null comment '主键'
        primary key,
    name varchar(32) null,
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null
);

create table t_school_license
(
    id bigint not null comment '主键'
        primary key,
    number varchar(32) null comment '证书编号',
    remark varchar(32) null comment '备注',
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null
)
comment '学校证书';

create table t_school
(
    id bigint not null comment '主键'
        primary key,
    name varchar(32) null comment '学校名称',
    build_date date null comment '建校日期',
    type varchar(16) null comment '学校性质 PRIVATE：私立；PUBLIC：公立',
    budget decimal(10,4) null comment '每年经费预算',
    score int null comment '专业数',
    address json null comment '学校地址',
    additional_info json null comment '其他信息',
    leadership_information_list json null comment '历届领导信息',
    awards_set varchar(32) null comment '历届获奖信息',
    score_list json null comment '历届学校评分',
    evaluate varchar(128) null comment '学校评价',
    school_license_id bigint null comment '证书信息',
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null,
    constraint t_school_t_school_license_id_fk
        foreign key (school_license_id) references t_school_license (id)
)
comment '学校';

create table t_school_student
(
    id bigint not null comment '主键'
        primary key,
    name varchar(32) null comment '姓名',
    grade int null comment '年级',
    sex varchar(16) null comment '性别',
    school_id bigint null,
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null
)
comment '学生';

create index t_school_student_t_school_id_fk
    on t_school_student (school_id);

create table t_school_teacher
(
    id bigint not null comment '主键'
        primary key,
    name varchar(32) null comment '姓名',
    age int null comment '年龄',
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null
)
comment '教师';

create table t_school_teacher_related
(
    school_id bigint not null,
    teacher_id bigint not null,
    is_deleted bit default b'0' not null,
    constraint t_school_teacher_related_pk
        unique (school_id, teacher_id),
    constraint t_school_teacher_related_t_school_id_fk
        foreign key (school_id) references t_school (id),
    constraint t_school_teacher_related_t_school_teacher_id_fk
        foreign key (teacher_id) references t_school_teacher (id)
);

create table t_tag
(
    id bigint not null comment '主键'
        primary key,
    title varchar(32) null,
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null
)
comment 'tag';

create table t_task
(
    id bigint not null comment '主键'
        primary key,
    task_name varchar(32) not null comment '任务名称',
    complete_time datetime null comment '完成时间',
    cost_hours int null,
    complete bit null,
    assign_user_id bigint null,
    address json null,
    status varchar(16) null comment '用户状态',
    list json null,
    phone_number varchar(32) null,
    map json null,
    list_map json null,
    num_list json null,
    str_list json null,
    dept text not null,
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null
)
comment '任务表';

create table t_user
(
    id bigint not null comment '主键'
        primary key,
    name varchar(32) not null comment '姓名',
    gender varchar(16) not null comment '性别',
    age int not null comment '年龄',
    birthday date null comment '出生时间',
    mobile varchar(32) not null comment '手机号码',
    email varchar(32) null comment '邮箱',
    native_place varchar(32) not null comment '籍贯',
    hobby json not null comment '兴趣爱好',
    marriage bit null comment '婚否',
    score decimal null,
    introduce text null,
    attachment text null,
    school_experience text null,
    status varchar(16) null comment '用户状态',
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null
)
comment '人员信息表';

create table t_worker
(
    id bigint auto_increment comment '主键'
        primary key,
    name varchar(32) null,
    created_by bigint null,
    created_at datetime null,
    updated_by bigint null,
    updated_at datetime null,
    is_deleted bit null
)
comment '工人';

