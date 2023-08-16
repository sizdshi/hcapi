-- 接口信息
create table if not exists hcapi.`interface_info`
(
    `id` bigint not null auto_increment comment '主键' primary key,
    `name` varchar(256) not null comment '接口名称',
    `description` varchar(256) null comment '描述',
    `requestParams` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求参数',
    `requestParamsRemark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求参数说明',
    `url` varchar(512) not null comment '接口地址',
    `requestHeader` text null comment '请求头',
    `responseHeader` text null comment '响应头',
    `status` int default 0 not null comment '接口状态（0-关闭，1-开启）',
    `method` varchar(256) not null comment '请求类型',
    `userId` bigint not null comment '创建人',
    `createTime` datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDelete` tinyint default 0 not null comment '是否删除(0-未删, 1-已删)'
    ) comment '接口信息';

insert into hcapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`) values ('孙鹏煊', '覃熠彤', 'www.carletta-white.biz', 'VC', '郑思远', 0, '高雪松', 77986963);
insert into hcapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`) values ('陆风华', '许伟诚', 'www.markus-dickinson.io', 'eg', '赵浩轩', 0, '夏金鑫', 4848);
insert into hcapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`) values ('贺梓晨', '廖明轩', 'www.haywood-smitham.biz', 'j3tu', '钱雨泽', 0, '罗子默', 35498);
insert into hcapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`) values ('洪晟睿', '彭雪松', 'www.thurman-mckenzie.name', 'HmzLW', '陶弘文', 0, '韦绍辉', 56);
insert into hcapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`) values ('薛琪', '邵鸿煊', 'www.saran-nolan.name', 'yyPy', '万越彬', 0, '白彬', 7135290991);
insert into hcapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`) values ('毛智渊', '廖弘文', 'www.cornell-hartmann.info', 'QSF', '吴煜城', 0, '梁弘文', 2694742);
insert into hcapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`) values ('赵涛', '江睿渊', 'www.kory-jaskolski.biz', 'QM', '覃明轩', 0, '马志泽', 1098);
insert into hcapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`) values ('谢越泽', '钱志强', 'www.lyda-hayes.com', 'xXen', '姚鹏涛', 0, '卢烨霖', 4312);
insert into hcapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`) values ('邹绍齐', '毛志泽', 'www.noe-ratke.info', 'nezM', '胡嘉懿', 0, '陶浩', 3613375);
insert into hcapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`) values ('金晓博', '万天翊', 'www.reda-padberg.com', 'eDC', '万立辉', 0, '邱子涵', 451525365);
insert into hcapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`) values ('林烨霖', '宋乐驹', 'www.kandi-johnston.io', 'fea1', '金文博', 0, '钱鑫鹏', 9);
insert into hcapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`) values ('谭涛', '孙浩轩', 'www.lavina-lakin.com', 'v3KUO', '苏立辉', 0, '吴雨泽', 38656);
insert into hcapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`) values ('龙天磊', '崔楷瑞', 'www.willene-morar.io', 'YqFRS', '万明哲', 0, '刘琪', 84420887);
insert into hcapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`) values ('崔潇然', '韩思', 'www.augusta-kris.name', 'HvBT', '熊思聪', 0, '李绍齐', 6451880913);
insert into hcapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`) values ('龚明轩', '覃伟宸', 'www.angelo-klocko.biz', 'PN8U', '方正豪', 0, '唐志泽', 9469270);
insert into hcapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`) values ('程智宸', '叶航', 'www.daryl-block.net', 'iaL', '姜立轩', 0, '程越彬', 36160);
insert into hcapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`) values ('白擎宇', '熊思源', 'www.zetta-marks.net', 'R9N', '严哲瀚', 0, '田明哲', 10007371);
insert into hcapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`) values ('白荣轩', '邓凯瑞', 'www.luis-shanahan.io', '6J4', '孙天翊', 0, '万鹏飞', 3067);
insert into hcapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`) values ('朱越泽', '傅健柏', 'www.fiona-auer.io', 'DK90', '罗潇然', 0, '孟健雄', 78585466);
insert into hcapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`) values ('崔苑博', '吕靖琪', 'www.chas-schmeler.biz', 'fuJ', '武凯瑞', 0, '莫雨泽', 369);