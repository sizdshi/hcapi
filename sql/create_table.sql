# 数据库初始化
#
#

-- 创建库
create database if not exists hcapi;

-- 切换库
use hcapi;

-- 用户表
create table if not exists user
(
    id             bigint auto_increment comment 'id' primary key,
    userAccount    varchar(256)                           not null comment '账号',
    userPassword   varchar(512)                           not null comment '密码',
    userName       varchar(256)                           null comment '用户昵称',
    userAvatar     varchar(1024)                          null comment '用户头像',
    userProfile    varchar(512)                           null comment '用户简介',
    userRole       varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    email          varchar(256)                           null comment '邮箱',
    gender         varchar(10)                            null comment '性别 0-男 1-女',
    balance        bigint       default 30                not null comment '钱包余额,注册送30币',
    invitationCode varchar(256)                           null comment '邀请码',
    status         tinyint      default 0                 not null comment '账号状态（0- 正常 1- 封号）',
    accessKey      varchar(512)                           null comment 'accessKey',
    secretKey      varchar(512)                           null comment 'secretKey',
    createTime     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint      default 0                 not null comment '是否删除',
    constraint uni_userAccount
        unique (userAccount)
) comment '用户' collate = utf8mb4_unicode_ci;

create table if not exists user_interface_info
(
    id              bigint auto_increment comment '主键'
        primary key,
    userId          bigint                             not null comment '调用用户 id',
    interfaceInfoId bigint                             not null comment '接口 id',
    totalNum        int      default 0                 not null comment '总调用次数',
    leftNum         int      default 0                 not null comment '剩余调用次数',
    status          int      default 0                 not null comment '0-正常，1-禁用',
    createTime      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint  default 0                 not null comment '是否删除(0-未删, 1-已删)'
)
    comment '用户调用接口关系';

