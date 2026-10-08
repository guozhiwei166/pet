-- =====================================================================
-- 基于 Spring Boot 的旅游民宿综合服务平台 - 建库脚本
-- MySQL 8.x / InnoDB / utf8mb4
-- 说明:
--   1. 主键统一 BIGINT 自增;所有业务表统一 create_time/update_time/deleted(逻辑删除)
--   2. 表间采用"逻辑外键 + 索引"(避免物理外键带来的初始化顺序与级联复杂度),
--      在论文中可说明按第三范式设计、外键约束由应用层与索引保证。
--   3. 金额 DECIMAL(10,2),经纬度 DECIMAL(10,6),状态一律语义化字符串/常量。
--   4. 收藏表为"临时业务",采用物理删除(便于唯一键复用),故无 deleted 字段。
-- =====================================================================

DROP DATABASE IF EXISTS tourism_homestay;
CREATE DATABASE tourism_homestay DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE tourism_homestay;

-- ---------------------------------------------------------------------
-- 1. 目的地 destination
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS destination;
CREATE TABLE destination (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name         VARCHAR(50)  NOT NULL COMMENT '目的地名称,如 大理',
    province     VARCHAR(50)  NOT NULL COMMENT '所属省份',
    city         VARCHAR(50)  NOT NULL COMMENT '所属城市',
    level        TINYINT      NOT NULL DEFAULT 2 COMMENT '级别 1=省份 2=城市/地区 3=区县',
    cover_image  VARCHAR(255) DEFAULT NULL COMMENT '封面图',
    description  VARCHAR(500) DEFAULT NULL COMMENT '目的地简介',
    scenic_tags  VARCHAR(255) DEFAULT NULL COMMENT '特色标签,逗号分隔,如 古城,洱海,苍山',
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1=展示 0=隐藏',
    sort_order   INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    create_time  DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time  DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    PRIMARY KEY (id)
) ENGINE = InnoDB COMMENT ='旅游目的地';

-- ---------------------------------------------------------------------
-- 2. 用户 user
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username     VARCHAR(50)  NOT NULL COMMENT '登录名',
    password     VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码',
    nickname     VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    phone        VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    avatar       VARCHAR(255) DEFAULT NULL COMMENT '头像',
    gender       TINYINT      NOT NULL DEFAULT 0 COMMENT '性别 0=未知 1=男 2=女',
    email        VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    role         VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色 USER=普通用户 MERCHANT=商家 ADMIN=管理员',
    status       TINYINT      NOT NULL DEFAULT 0 COMMENT '状态 0=正常 1=禁用',
    create_time  DATETIME     DEFAULT NULL COMMENT '注册时间',
    update_time  DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_role (role)
) ENGINE = InnoDB COMMENT ='用户表(含普通用户/商家账号/管理员)';

-- ---------------------------------------------------------------------
-- 3. 商家 merchant
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS merchant;
CREATE TABLE merchant (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id        BIGINT       NOT NULL COMMENT '关联 user.id(该用户 role=MERCHANT)',
    merchant_name  VARCHAR(100) NOT NULL COMMENT '商家名称',
    contact_name   VARCHAR(50)  DEFAULT NULL COMMENT '联系人姓名',
    contact_phone  VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    license_no     VARCHAR(100) DEFAULT NULL COMMENT '营业执照号',
    description    VARCHAR(1000) DEFAULT NULL COMMENT '商家简介',
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '审核状态 PENDING=待审核 APPROVED=已通过 REJECTED=已驳回 DISABLED=已禁用',
    audit_time     DATETIME     DEFAULT NULL COMMENT '审核时间',
    audit_remark   VARCHAR(255) DEFAULT NULL COMMENT '审核备注',
    create_time    DATETIME     DEFAULT NULL COMMENT '申请时间',
    update_time    DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_user (user_id),
    KEY idx_status (status)
) ENGINE = InnoDB COMMENT ='民宿商家';

-- ---------------------------------------------------------------------
-- 4. 民宿 homestay
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS homestay;
CREATE TABLE homestay (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    merchant_id     BIGINT        NOT NULL COMMENT '所属商家 merchant.id',
    destination_id  BIGINT        NOT NULL COMMENT '所属目的地 destination.id',
    name            VARCHAR(100)  NOT NULL COMMENT '民宿名称',
    cover_image     VARCHAR(255)  DEFAULT NULL COMMENT '封面图 URL',
    address         VARCHAR(255)  DEFAULT NULL COMMENT '详细地址',
    longitude       DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
    latitude        DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
    intro           VARCHAR(500)  DEFAULT NULL COMMENT '一句话简介',
    description     TEXT COMMENT '详细介绍',
    features        VARCHAR(500)  DEFAULT NULL COMMENT '民宿特色,逗号分隔,如 海景,庭院,亲子',
    facilities      VARCHAR(500)  DEFAULT NULL COMMENT '配套设施,逗号分隔,如 WiFi,停车场,接机',
    phone           VARCHAR(20)   DEFAULT NULL COMMENT '联系电话',
    business_status VARCHAR(20)   NOT NULL DEFAULT 'OPEN' COMMENT '营业状态 OPEN=营业中 CLOSED=歇业',
    audit_status    VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '审核状态 PENDING=待审核 APPROVED=已通过 REJECTED=已拒绝 OFFLINE=已下架',
    rating          DECIMAL(3,1)  NOT NULL DEFAULT 5.0 COMMENT '综合评分 1-5',
    rating_count    INT           NOT NULL DEFAULT 0 COMMENT '评价人数',
    favorite_count  INT           NOT NULL DEFAULT 0 COMMENT '收藏数量',
    view_count      INT           NOT NULL DEFAULT 0 COMMENT '浏览数量',
    min_price       DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '最低房价(冗余,列表/推荐用)',
    sort_order      INT           NOT NULL DEFAULT 0 COMMENT '排序号',
    audit_time      DATETIME      DEFAULT NULL COMMENT '审核时间',
    audit_remark    VARCHAR(255)  DEFAULT NULL COMMENT '审核备注',
    create_time     DATETIME      DEFAULT NULL COMMENT '创建时间',
    update_time     DATETIME      DEFAULT NULL COMMENT '更新时间',
    deleted         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_merchant (merchant_id),
    KEY idx_destination (destination_id),
    KEY idx_audit_status (audit_status)
) ENGINE = InnoDB COMMENT ='民宿';

-- ---------------------------------------------------------------------
-- 5. 民宿图片 homestay_image
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS homestay_image;
CREATE TABLE homestay_image (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    homestay_id BIGINT       NOT NULL COMMENT '民宿 id',
    url         VARCHAR(255) NOT NULL COMMENT '图片 URL',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_homestay (homestay_id)
) ENGINE = InnoDB COMMENT ='民宿图片';

-- ---------------------------------------------------------------------
-- 6. 房型 room_type
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS room_type;
CREATE TABLE room_type (
    id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    homestay_id    BIGINT        NOT NULL COMMENT '所属民宿 id',
    name           VARCHAR(50)   NOT NULL COMMENT '房型名称,如 大床房',
    cover_image    VARCHAR(255)  DEFAULT NULL COMMENT '房型图片',
    bed_type       VARCHAR(50)   DEFAULT NULL COMMENT '床型,如 1.8米大床',
    max_guests     INT           NOT NULL DEFAULT 2 COMMENT '可入住人数',
    area           DECIMAL(8,2)  DEFAULT NULL COMMENT '面积(㎡)',
    has_breakfast  TINYINT       NOT NULL DEFAULT 0 COMMENT '是否含早餐 0=否 1=是',
    price          DECIMAL(10,2) NOT NULL COMMENT '单价(元/晚)',
    room_count     INT           NOT NULL DEFAULT 1 COMMENT '该房型房间总数',
    stock          INT           NOT NULL DEFAULT 0 COMMENT '剩余可售总间数(冗余展示,实际扣减以日期库存为准)',
    intro          VARCHAR(1000) DEFAULT NULL COMMENT '房型介绍',
    facilities     VARCHAR(500)  DEFAULT NULL COMMENT '房内设施,逗号分隔',
    status         VARCHAR(20)   NOT NULL DEFAULT 'ON' COMMENT '状态 ON=上架 OFF=下架',
    create_time    DATETIME      DEFAULT NULL COMMENT '创建时间',
    update_time    DATETIME      DEFAULT NULL COMMENT '更新时间',
    deleted        TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_homestay (homestay_id)
) ENGINE = InnoDB COMMENT ='民宿房型';

-- ---------------------------------------------------------------------
-- 7. 物理房间 hotel_room(供商家"房间管理"录入参考,不强制逐日录入)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS hotel_room;
CREATE TABLE hotel_room (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    homestay_id  BIGINT       NOT NULL COMMENT '民宿 id',
    room_type_id BIGINT       NOT NULL COMMENT '房型 id',
    room_number  VARCHAR(20)  NOT NULL COMMENT '房间号,如 201',
    floor        VARCHAR(20)  DEFAULT NULL COMMENT '所在楼层',
    remark       VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time  DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time  DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_homestay (homestay_id),
    KEY idx_room_type (room_type_id)
) ENGINE = InnoDB COMMENT ='物理房间(简化管理)';

-- ---------------------------------------------------------------------
-- 8. 房型日期库存 room_inventory —— 防超卖核心表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS room_inventory;
CREATE TABLE room_inventory (
    id              BIGINT  NOT NULL AUTO_INCREMENT COMMENT '主键',
    room_type_id    BIGINT  NOT NULL COMMENT '房型 id',
    stay_date       DATE    NOT NULL COMMENT '日期(入住那晚)',
    available_count INT     NOT NULL DEFAULT 0 COMMENT '该日剩余可售间数',
    create_time     DATETIME DEFAULT NULL COMMENT '创建时间',
    update_time     DATETIME DEFAULT NULL COMMENT '更新时间',
    deleted         TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_room_date (room_type_id, stay_date)
) ENGINE = InnoDB COMMENT ='房型日期库存';

-- ---------------------------------------------------------------------
-- 9-11. 旅游资源:景点 / 旅游体验 / 餐饮(由平台管理员发布维护,status 上下架)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS scenic_spot;
CREATE TABLE scenic_spot (
    id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    destination_id BIGINT        NOT NULL COMMENT '所属目的地',
    name           VARCHAR(100)  NOT NULL COMMENT '景点名称',
    cover_image    VARCHAR(255)  DEFAULT NULL COMMENT '封面图',
    images         TEXT COMMENT '图集 JSON 数组',
    intro          VARCHAR(500)  DEFAULT NULL COMMENT '简介',
    description    TEXT COMMENT '详细介绍',
    address        VARCHAR(255)  DEFAULT NULL COMMENT '地址',
    longitude      DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
    latitude       DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
    price          DECIMAL(10,2) DEFAULT NULL COMMENT '门票/人均价(元),0 表示免费',
    rating         DECIMAL(3,1)  NOT NULL DEFAULT 5.0 COMMENT '评分',
    tags           VARCHAR(255)  DEFAULT NULL COMMENT '标签,逗号分隔',
    favorite_count INT           NOT NULL DEFAULT 0 COMMENT '收藏数',
    status         TINYINT       NOT NULL DEFAULT 1 COMMENT '状态 1=上架 0=下架',
    create_time    DATETIME      DEFAULT NULL COMMENT '创建时间',
    update_time    DATETIME      DEFAULT NULL COMMENT '更新时间',
    deleted        TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_destination (destination_id)
) ENGINE = InnoDB COMMENT ='景点';

DROP TABLE IF EXISTS tourism_experience;
CREATE TABLE tourism_experience (
    id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    destination_id BIGINT        NOT NULL COMMENT '所属目的地',
    name           VARCHAR(100)  NOT NULL COMMENT '体验名称',
    cover_image    VARCHAR(255)  DEFAULT NULL COMMENT '封面图',
    images         TEXT COMMENT '图集 JSON 数组',
    intro          VARCHAR(500)  DEFAULT NULL COMMENT '简介',
    description    TEXT COMMENT '详细介绍',
    address        VARCHAR(255)  DEFAULT NULL COMMENT '地址',
    longitude      DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
    latitude       DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
    price          DECIMAL(10,2) DEFAULT NULL COMMENT '人均价格(元)',
    rating         DECIMAL(3,1)  NOT NULL DEFAULT 5.0 COMMENT '评分',
    tags           VARCHAR(255)  DEFAULT NULL COMMENT '标签,逗号分隔',
    favorite_count INT           NOT NULL DEFAULT 0 COMMENT '收藏数',
    status         TINYINT       NOT NULL DEFAULT 1 COMMENT '状态 1=上架 0=下架',
    create_time    DATETIME      DEFAULT NULL COMMENT '创建时间',
    update_time    DATETIME      DEFAULT NULL COMMENT '更新时间',
    deleted        TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_destination (destination_id)
) ENGINE = InnoDB COMMENT ='旅游体验';

DROP TABLE IF EXISTS restaurant;
CREATE TABLE restaurant (
    id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    destination_id BIGINT        NOT NULL COMMENT '所属目的地',
    name           VARCHAR(100)  NOT NULL COMMENT '餐饮名称',
    cover_image    VARCHAR(255)  DEFAULT NULL COMMENT '封面图',
    images         TEXT COMMENT '图集 JSON 数组',
    intro          VARCHAR(500)  DEFAULT NULL COMMENT '简介',
    description    TEXT COMMENT '详细介绍',
    address        VARCHAR(255)  DEFAULT NULL COMMENT '地址',
    longitude      DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
    latitude       DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
    price          DECIMAL(10,2) DEFAULT NULL COMMENT '人均价格(元)',
    rating         DECIMAL(3,1)  NOT NULL DEFAULT 5.0 COMMENT '评分',
    tags           VARCHAR(255)  DEFAULT NULL COMMENT '标签,逗号分隔',
    favorite_count INT           NOT NULL DEFAULT 0 COMMENT '收藏数',
    status         TINYINT       NOT NULL DEFAULT 1 COMMENT '状态 1=上架 0=下架',
    create_time    DATETIME      DEFAULT NULL COMMENT '创建时间',
    update_time    DATETIME      DEFAULT NULL COMMENT '更新时间',
    deleted        TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_destination (destination_id)
) ENGINE = InnoDB COMMENT ='餐饮';

-- ---------------------------------------------------------------------
-- 12-14. 民宿与旅游资源关联表(创新点①:民宿为中心聚合周边资源)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS homestay_scenic;
CREATE TABLE homestay_scenic (
    id          BIGINT  NOT NULL AUTO_INCREMENT COMMENT '主键',
    homestay_id BIGINT  NOT NULL COMMENT '民宿 id',
    scenic_id   BIGINT  NOT NULL COMMENT '景点 id',
    sort_order  INT     NOT NULL DEFAULT 0 COMMENT '排序号',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    deleted     TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hs (homestay_id, scenic_id),
    KEY idx_scenic (scenic_id)
) ENGINE = InnoDB COMMENT ='民宿-景点关联';

DROP TABLE IF EXISTS homestay_experience;
CREATE TABLE homestay_experience (
    id            BIGINT  NOT NULL AUTO_INCREMENT COMMENT '主键',
    homestay_id   BIGINT  NOT NULL COMMENT '民宿 id',
    experience_id BIGINT  NOT NULL COMMENT '体验 id',
    sort_order    INT     NOT NULL DEFAULT 0 COMMENT '排序号',
    create_time   DATETIME DEFAULT NULL COMMENT '创建时间',
    update_time   DATETIME DEFAULT NULL COMMENT '更新时间',
    deleted       TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_he (homestay_id, experience_id),
    KEY idx_experience (experience_id)
) ENGINE = InnoDB COMMENT ='民宿-体验关联';

DROP TABLE IF EXISTS homestay_restaurant;
CREATE TABLE homestay_restaurant (
    id            BIGINT  NOT NULL AUTO_INCREMENT COMMENT '主键',
    homestay_id   BIGINT  NOT NULL COMMENT '民宿 id',
    restaurant_id BIGINT  NOT NULL COMMENT '餐饮 id',
    sort_order    INT     NOT NULL DEFAULT 0 COMMENT '排序号',
    create_time   DATETIME DEFAULT NULL COMMENT '创建时间',
    update_time   DATETIME DEFAULT NULL COMMENT '更新时间',
    deleted       TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hr (homestay_id, restaurant_id),
    KEY idx_restaurant (restaurant_id)
) ENGINE = InnoDB COMMENT ='民宿-餐饮关联';

-- ---------------------------------------------------------------------
-- 15. 预订订单 orders
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS orders;
CREATE TABLE orders (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_no        VARCHAR(32)   NOT NULL COMMENT '订单编号',
    user_id         BIGINT        NOT NULL COMMENT '下单用户 id',
    homestay_id     BIGINT        NOT NULL COMMENT '民宿 id',
    room_type_id    BIGINT        NOT NULL COMMENT '房型 id',
    homestay_name   VARCHAR(100)  DEFAULT NULL COMMENT '民宿名称(快照)',
    homestay_cover  VARCHAR(255)  DEFAULT NULL COMMENT '民宿封面(快照)',
    room_type_name  VARCHAR(50)   DEFAULT NULL COMMENT '房型名称(快照)',
    check_in_date   DATE          NOT NULL COMMENT '入住日期',
    check_out_date  DATE          NOT NULL COMMENT '退房日期',
    nights          INT           NOT NULL COMMENT '入住晚数',
    room_count      INT           NOT NULL DEFAULT 1 COMMENT '房间数量',
    unit_price      DECIMAL(10,2) NOT NULL COMMENT '房型单价(快照)',
    total_amount    DECIMAL(10,2) NOT NULL COMMENT '房费总价 = 晚数*单价*间数',
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
    pay_amount      DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    contact_name    VARCHAR(50)   DEFAULT NULL COMMENT '联系人',
    contact_phone   VARCHAR(20)   DEFAULT NULL COMMENT '联系电话',
    guest_names     VARCHAR(255)  DEFAULT NULL COMMENT '入住人姓名(可多个)',
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '订单状态 PENDING=待支付 PAID=已支付/待入住 CHECKED_IN=入住中 COMPLETED=已完成 CANCELLED=已取消',
    pay_status      VARCHAR(20)   NOT NULL DEFAULT 'UNPAID' COMMENT '支付状态 UNPAID=未支付 PAID=已支付 REFUNDED=已退款',
    pay_type        VARCHAR(20)   DEFAULT NULL COMMENT '支付方式 WECHAT=微信 ALIPAY=支付宝 BANK=银行卡',
    remark          VARCHAR(255)  DEFAULT NULL COMMENT '订单备注',
    cancel_time     DATETIME      DEFAULT NULL COMMENT '取消时间',
    pay_time        DATETIME      DEFAULT NULL COMMENT '支付时间',
    check_in_time   DATETIME      DEFAULT NULL COMMENT '实际入住时间',
    check_out_time  DATETIME      DEFAULT NULL COMMENT '实际退房时间',
    complete_time   DATETIME      DEFAULT NULL COMMENT '完成时间',
    create_time     DATETIME      DEFAULT NULL COMMENT '下单时间',
    update_time     DATETIME      DEFAULT NULL COMMENT '更新时间',
    deleted         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user (user_id),
    KEY idx_homestay (homestay_id),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB COMMENT ='预订订单';

-- ---------------------------------------------------------------------
-- 16. 收藏 favorite(物理删除,保证唯一键可复用)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS favorite;
CREATE TABLE favorite (
    id          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT NOT NULL COMMENT '用户 id',
    target_type TINYINT NOT NULL COMMENT '收藏类型 1=民宿 2=景点 3=体验 4=餐饮',
    target_id   BIGINT NOT NULL COMMENT '目标 id',
    create_time DATETIME DEFAULT NULL COMMENT '收藏时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_target (user_id, target_type, target_id),
    KEY idx_target (target_type, target_id)
) ENGINE = InnoDB COMMENT ='收藏';

-- ---------------------------------------------------------------------
-- 17. 评价 review(一个订单最多一条,order_id 唯一)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS review;
CREATE TABLE review (
    id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_id       BIGINT        NOT NULL COMMENT '订单 id',
    user_id        BIGINT        NOT NULL COMMENT '评价用户',
    homestay_id    BIGINT        NOT NULL COMMENT '民宿 id',
    rating         TINYINT       NOT NULL DEFAULT 5 COMMENT '总评分 1-5',
    hygiene_score  TINYINT       NOT NULL DEFAULT 5 COMMENT '卫生评分',
    service_score  TINYINT       NOT NULL DEFAULT 5 COMMENT '服务评分',
    facility_score TINYINT       NOT NULL DEFAULT 5 COMMENT '设施评分',
    location_score TINYINT       NOT NULL DEFAULT 5 COMMENT '位置评分',
    content        VARCHAR(1000) DEFAULT NULL COMMENT '评价内容',
    images         TEXT COMMENT '评价图片 JSON 数组',
    merchant_reply VARCHAR(1000) DEFAULT NULL COMMENT '商家回复',
    reply_time     DATETIME      DEFAULT NULL COMMENT '回复时间',
    status         TINYINT       NOT NULL DEFAULT 0 COMMENT '状态 0=正常 1=管理员隐藏(违规)',
    create_time    DATETIME      DEFAULT NULL COMMENT '评价时间',
    update_time    DATETIME      DEFAULT NULL COMMENT '更新时间',
    deleted        TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order (order_id),
    KEY idx_homestay (homestay_id),
    KEY idx_user (user_id)
) ENGINE = InnoDB COMMENT ='民宿评价';

-- ---------------------------------------------------------------------
-- 18. 举报 report
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS report;
CREATE TABLE report (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    report_type   TINYINT      NOT NULL COMMENT '举报类型 1=民宿 2=评价 3=体验 4=餐饮',
    target_id     BIGINT       NOT NULL COMMENT '被举报对象 id',
    reason        VARCHAR(50)  DEFAULT NULL COMMENT '举报原因:虚假信息/违规内容/价格问题/服务问题/其他',
    description   VARCHAR(500) DEFAULT NULL COMMENT '举报说明',
    reporter_id   BIGINT       NOT NULL COMMENT '举报人 user.id',
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '处理状态 PENDING=待处理 HANDLING=处理中 HANDLED=已处理 REJECTED=驳回',
    handle_time   DATETIME     DEFAULT NULL COMMENT '处理时间',
    handle_remark VARCHAR(255) DEFAULT NULL COMMENT '处理备注',
    create_time   DATETIME     DEFAULT NULL COMMENT '举报时间',
    update_time   DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_reporter (reporter_id),
    KEY idx_status (status),
    KEY idx_target (report_type, target_id)
) ENGINE = InnoDB COMMENT ='举报';

-- ---------------------------------------------------------------------
-- 19. 旅行计划 travel_plan(创新点②)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS travel_plan;
CREATE TABLE travel_plan (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id          BIGINT       NOT NULL COMMENT '所属用户',
    plan_name        VARCHAR(100) NOT NULL COMMENT '计划名称,如 大理三日游',
    destination_id   BIGINT       DEFAULT NULL COMMENT '目的地 id',
    destination_name VARCHAR(50)  DEFAULT NULL COMMENT '目的地名称(快照)',
    start_date       DATE         DEFAULT NULL COMMENT '开始日期',
    end_date         DATE         DEFAULT NULL COMMENT '结束日期',
    intro            VARCHAR(500) DEFAULT NULL COMMENT '计划简介',
    status           VARCHAR(20)  NOT NULL DEFAULT 'ONGOING' COMMENT '状态 ONGOING=进行中 FINISHED=已完成 CANCELLED=已取消',
    create_time      DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time      DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted          TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE = InnoDB COMMENT ='个人旅行计划';

-- ---------------------------------------------------------------------
-- 20. 旅行计划行程项目 travel_plan_item
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS travel_plan_item;
CREATE TABLE travel_plan_item (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    plan_id     BIGINT       NOT NULL COMMENT '所属计划 id',
    day_number  INT          NOT NULL DEFAULT 1 COMMENT '第几天(从1开始)',
    item_type   VARCHAR(20)  NOT NULL COMMENT '项目类型 HOMESTAY=民宿 SCENIC=景点 EXPERIENCE=体验 RESTAURANT=餐饮',
    item_id     BIGINT       NOT NULL COMMENT '项目 id(对应类型表)',
    item_name   VARCHAR(100) DEFAULT NULL COMMENT '项目名称(快照,冗余)',
    start_time  TIME         DEFAULT NULL COMMENT '开始时间(可选)',
    end_time    TIME         DEFAULT NULL COMMENT '结束时间(可选)',
    remark      VARCHAR(255) DEFAULT NULL COMMENT '备注',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序号(同天内)',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_plan (plan_id)
) ENGINE = InnoDB COMMENT ='旅行计划行程项目';

-- ---------------------------------------------------------------------
-- 21. 首页轮播 banner
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS banner;
CREATE TABLE banner (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title       VARCHAR(100) DEFAULT NULL COMMENT '标题',
    image_url   VARCHAR(255) NOT NULL COMMENT '图片 URL',
    link_type   VARCHAR(20)  NOT NULL DEFAULT 'NONE' COMMENT '跳转类型 NONE=无 HOMESTAY=民宿 SCENIC=景点',
    link_id     BIGINT       DEFAULT NULL COMMENT '跳转目标 id',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1=展示 0=隐藏',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id)
) ENGINE = InnoDB COMMENT ='首页轮播图';

-- ---------------------------------------------------------------------
-- 22. 系统配置 sys_setting
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS sys_setting;
CREATE TABLE sys_setting (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    setting_key   VARCHAR(50)  NOT NULL COMMENT '配置键',
    setting_value VARCHAR(1000) DEFAULT NULL COMMENT '配置值',
    remark        VARCHAR(255) DEFAULT NULL COMMENT '备注',
    update_time   DATETIME     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_key (setting_key)
) ENGINE = InnoDB COMMENT ='系统基础配置';

-- 初始化基础配置
INSERT INTO sys_setting (setting_key, setting_value, remark) VALUES
('platform_name', '云栖民宿', '平台名称'),
('platform_slogan', '住进风景里,玩转目的地', '平台标语'),
('platform_notice', '欢迎来到云栖民宿!平台提供民宿预订、周边景点/体验/餐饮推荐与个人旅行计划服务,祝您旅途愉快~', '站点公告'),
('merchant_apply_enabled', 'true', '是否开放商家入驻');
