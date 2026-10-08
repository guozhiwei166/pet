-- ============================================================
-- PawStay 毛孩驿站 · 宠物寄养管理系统（毕业设计）
-- 数据库结构：MySQL 8.x
-- 说明：开发/演示模式下每次启动由 Spring SQL Init 重建数据；
--      答辩录入真实数据后请将 application-mysql.yml 中
--      spring.sql.init.mode 改为 never
-- ============================================================
SET NAMES utf8mb4;

DROP TABLE IF EXISTS pet_message;
DROP TABLE IF EXISTS pet_review;
DROP TABLE IF EXISTS pet_care_record;
DROP TABLE IF EXISTS pet_order;
DROP TABLE IF EXISTS pet_room;
DROP TABLE IF EXISTS pet_pet;
DROP TABLE IF EXISTS pet_merchant;
DROP TABLE IF EXISTS pet_user;

-- 用户表：三角色（OWNER 宠物主人 / MERCHANT 商家 / ADMIN 平台管理员）
CREATE TABLE pet_user (
  id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
  phone       VARCHAR(20)  NOT NULL,
  password    VARCHAR(64)  NOT NULL COMMENT 'SHA-256(盐+原文)',
  nickname    VARCHAR(50),
  avatar      VARCHAR(255),
  role        VARCHAR(20)  NOT NULL DEFAULT 'OWNER',
  status      TINYINT      DEFAULT 1 COMMENT '1 正常 0 停用',
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_user_phone ON pet_user (phone);

-- 商家（店铺）表
CREATE TABLE pet_merchant (
  id             BIGINT        AUTO_INCREMENT PRIMARY KEY,
  user_id        BIGINT        NOT NULL COMMENT '关联登录账号',
  shop_name      VARCHAR(100)  NOT NULL,
  address        VARCHAR(255),
  phone          VARCHAR(20),
  license        VARCHAR(255)  COMMENT '资质说明',
  description    VARCHAR(1000),
  tags           VARCHAR(255)  COMMENT '特色标签，逗号分隔',
  service_species VARCHAR(50)  COMMENT '可寄养物种 CAT/DOG',
  rating         DECIMAL(3,1)  DEFAULT 5.0,
  reply_rate     INT           DEFAULT 100 COMMENT '回复率 %',
  audit_status   VARCHAR(20)   DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  audit_reason   VARCHAR(255),
  boarding_count INT           DEFAULT 0 COMMENT '累计寄养单数',
  open_status    TINYINT       DEFAULT 1,
  create_time    DATETIME      DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_merchant_user ON pet_merchant (user_id);

-- 宠物档案表
CREATE TABLE pet_pet (
  id           BIGINT        AUTO_INCREMENT PRIMARY KEY,
  user_id      BIGINT        NOT NULL,
  name         VARCHAR(50)   NOT NULL,
  species      VARCHAR(10)   NOT NULL COMMENT 'CAT/DOG',
  breed        VARCHAR(50),
  gender       VARCHAR(10)   COMMENT 'M/F',
  birth_date   DATE,
  weight       DECIMAL(5,2)  COMMENT 'kg',
  sterilized   TINYINT       DEFAULT 0 COMMENT '1 已绝育',
  temperament  VARCHAR(20)   COMMENT '温顺/活泼/警惕',
  aggressive   TINYINT       DEFAULT 0 COMMENT '1 有攻击性（排笼约束）',
  vaccinated   TINYINT       DEFAULT 0,
  vaccine_note VARCHAR(255),
  photo        TEXT          COMMENT 'Base64，生产建议对象存储',
  notes        VARCHAR(500),
  create_time  DATETIME      DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_pet_user ON pet_pet (user_id);

-- 笼位/房型表
CREATE TABLE pet_room (
  id               BIGINT        AUTO_INCREMENT PRIMARY KEY,
  merchant_id      BIGINT        NOT NULL,
  name             VARCHAR(100)  NOT NULL,
  species          VARCHAR(10)   DEFAULT 'ALL' COMMENT 'CAT/DOG/ALL',
  max_weight       DECIMAL(5,2)  COMMENT '接收体重上限 kg',
  need_sterilized  TINYINT       DEFAULT 0 COMMENT '1 要求已绝育',
  allow_aggressive TINYINT       DEFAULT 0 COMMENT '1 可接收有攻击性宠物',
  total_count      INT           DEFAULT 1 COMMENT '同型笼位数',
  price_per_day    DECIMAL(10,2) NOT NULL,
  photo            TEXT,
  tags             VARCHAR(255),
  description      VARCHAR(500),
  status           TINYINT       DEFAULT 1,
  create_time      DATETIME      DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_room_merchant ON pet_room (merchant_id);

-- 寄养订单表（状态机：待确认→已接单→寄养中→已完成，另有取消/拒单）
CREATE TABLE pet_order (
  id               BIGINT        AUTO_INCREMENT PRIMARY KEY,
  order_no         VARCHAR(32)   NOT NULL,
  user_id          BIGINT        NOT NULL,
  pet_id           BIGINT        NOT NULL,
  merchant_id      BIGINT        NOT NULL,
  room_id          BIGINT        NOT NULL,
  start_date       DATE          NOT NULL COMMENT '入住日',
  end_date         DATE          NOT NULL COMMENT '离店日',
  days             INT,
  price_per_day    DECIMAL(10,2),
  total_amount     DECIMAL(10,2),
  status           VARCHAR(30)   NOT NULL,
  contact_name     VARCHAR(50),
  contact_phone    VARCHAR(20),
  remark           VARCHAR(500),
  agreement_signed TINYINT       DEFAULT 0 COMMENT '1 已同意电子寄养协议',
  pay_time         DATETIME,
  checkin_time     DATETIME,
  checkout_time    DATETIME,
  weight_in        DECIMAL(5,2)  COMMENT '入住称重',
  weight_out       DECIMAL(5,2)  COMMENT '离店称重（健康对比）',
  reject_reason    VARCHAR(255),
  create_time      DATETIME      DEFAULT CURRENT_TIMESTAMP,
  update_time      DATETIME
);
CREATE INDEX idx_order_user ON pet_order (user_id);
CREATE INDEX idx_order_merchant ON pet_order (merchant_id);
CREATE INDEX idx_order_room ON pet_order (room_id);
CREATE INDEX idx_order_status ON pet_order (status);

-- 寄养日记表（商家每日护理打卡，本系统特色数据）
CREATE TABLE pet_care_record (
  id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
  order_id    BIGINT        NOT NULL,
  merchant_id BIGINT        NOT NULL,
  record_date DATE          NOT NULL,
  meal_done   TINYINT       DEFAULT 0,
  walk_done   TINYINT       DEFAULT 0,
  mood        VARCHAR(20),
  content     VARCHAR(1000),
  photos      TEXT          COMMENT '多张 Base64 逗号分隔',
  ai_summary  VARCHAR(1000) COMMENT 'AI 摘要（创新点）',
  weight      DECIMAL(5,2),
  create_time DATETIME      DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_care_order ON pet_care_record (order_id);

-- 评价表
CREATE TABLE pet_review (
  id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
  order_id    BIGINT       NOT NULL,
  user_id     BIGINT       NOT NULL,
  merchant_id BIGINT       NOT NULL,
  rating      INT          NOT NULL COMMENT '1-5 星',
  content     VARCHAR(500),
  photos      TEXT,
  reply       VARCHAR(500) COMMENT '商家回复',
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_review_merchant ON pet_review (merchant_id);

-- 站内消息表
CREATE TABLE pet_message (
  id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT       NOT NULL,
  title       VARCHAR(100),
  content     VARCHAR(500),
  type        VARCHAR(20)  DEFAULT 'SYSTEM' COMMENT 'ORDER/DIARY/SYSTEM',
  is_read     TINYINT      DEFAULT 0,
  ref_id      BIGINT       COMMENT '关联业务 id（如订单）',
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_msg_user ON pet_message (user_id);
