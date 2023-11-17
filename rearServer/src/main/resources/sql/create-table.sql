-- 创建工具表
create table IF NOT EXISTS fakeapitool
(
    name       varchar(20) not null comment 'token名称'
        primary key,
    value      text        not null comment 'token值',
    userName   varchar(50) not null comment 'token用户名',
    password   varchar(50) not null comment 'token密码',
    updateTime datetime    null comment 'token注册时间',
    constraint fakeApiTool_pk
        unique (name)
);

-- 创建用户表
create table IF NOT EXISTS fakeapitooluser
(
    name     varchar(20) default 'root'   not null comment '登录用户名',
    password varchar(50) default '123456' not null comment '登录密码',
    id       int         default 1        not null comment '用户Id'
        primary key,
    constraint fakeapitooluser_pk
        unique (name),
    constraint fakeapitooluser_pk2
        unique (name),
    constraint fakeapitooluser_pk3
        unique (id),
    constraint fakeapitooluser_pk4
        unique (name)
);

-- 用户表插入用户名和密码
INSERT IGNORE INTO oneapi.fakeapitooluser (name, password, id) VALUES ('root', '123456', 1);
