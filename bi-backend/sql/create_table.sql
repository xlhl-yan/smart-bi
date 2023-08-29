# 数据库初始化

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_userAccount (userAccount)
) comment '用户' collate = utf8mb4_unicode_ci;


-- auto-generated definition
create table chart
(
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(512)                       null comment '图表名称',
    goal        varchar(256)                       null comment '分析目标',
    execMessage text                               null comment '执行信息 主要保存失败信息',
    status      tinyint  default 0                 null comment '任务的状态 0-成功 1-失败 2-执行中 3-未执行',
    chartData   text                               null comment '图标原始信息',
    chartType   varchar(128)                       null comment '图标信息',
    userId      bigint                             null comment '创建人id',
    genChart    text                               null comment 'AI 生成图表信息',
    genResult   text                               null comment 'AI 生成分析结论',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除'
) comment '图标信息表' collate = utf8mb4_unicode_ci;


