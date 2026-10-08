-- ============================================================
-- PawStay 毛孩驿站 · 数据库结构（H2 内存库备用档，MODE=MySQL）
-- 与 schema-mysql.sql 等价，仅 TEXT→CLOB 差异
-- ============================================================

DROP TABLE IF EXISTS pet_message;
DROP TABLE IF EXISTS pet_review;
DROP TABLE IF EXISTS pet_care_record;
DROP TABLE IF EXISTS pet_order;
DROP TABLE IF EXISTS pet_room;
DROP TABLE IF EXISTS pet_pet;
DROP TABLE IF EXISTS pet_merchant;
DROP TABLE IF EXISTS pet_user;

CREATE TABLE pet_user (
  id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
  phone       VARCHAR(20)  NOT NULL,
  password    VARCHAR(64)  NOT NULL,
  nickname    VARCHAR(50),
  avatar      VARCHAR(255),
  role        VARCHAR(20)  NOT NULL DEFAULT 'OWNER',
  status      TINYINT      DEFAULT 1,
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_user_phone ON pet_user (phone);

CREATE TABLE pet_merchant (
  id             BIGINT        AUTO_INCREMENT PRIMARY KEY,
  user_id        BIGINT        NOT NULL,
  shop_name      VARCHAR(100)  NOT NULL,
  address        VARCHAR(255),
  phone          VARCHAR(20),
  license        VARCHAR(255),
  description    VARCHAR(1000),
  tags           VARCHAR(255),
  service_species VARCHAR(50),
  rating         DECIMAL(3,1)  DEFAULT 5.0,
  reply_rate     INT           DEFAULT 100,
  audit_status   VARCHAR(20)   DEFAULT 'PENDING',
  audit_reason   VARCHAR(255),
  boarding_count INT           DEFAULT 0,
  open_status    TINYINT       DEFAULT 1,
  create_time    DATETIME      DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_merchant_user ON pet_merchant (user_id);

CREATE TABLE pet_pet (
  id           BIGINT        AUTO_INCREMENT PRIMARY KEY,
  user_id      BIGINT        NOT NULL,
  name         VARCHAR(50)   NOT NULL,
  species      VARCHAR(10)   NOT NULL,
  breed        VARCHAR(50),
  gender       VARCHAR(10),
  birth_date   DATE,
  weight       DECIMAL(5,2),
  sterilized   TINYINT       DEFAULT 0,
  temperament  VARCHAR(20),
  aggressive   TINYINT       DEFAULT 0,
  vaccinated   TINYINT       DEFAULT 0,
  vaccine_note VARCHAR(255),
  photo        CLOB,
  notes        VARCHAR(500),
  create_time  DATETIME      DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_pet_user ON pet_pet (user_id);

CREATE TABLE pet_room (
  id               BIGINT        AUTO_INCREMENT PRIMARY KEY,
  merchant_id      BIGINT        NOT NULL,
  name             VARCHAR(100)  NOT NULL,
  species          VARCHAR(10)   DEFAULT 'ALL',
  max_weight       DECIMAL(5,2),
  need_sterilized  TINYINT       DEFAULT 0,
  allow_aggressive TINYINT       DEFAULT 0,
  total_count      INT           DEFAULT 1,
  price_per_day    DECIMAL(10,2) NOT NULL,
  photo            CLOB,
  tags             VARCHAR(255),
  description      VARCHAR(500),
  status           TINYINT       DEFAULT 1,
  create_time      DATETIME      DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_room_merchant ON pet_room (merchant_id);

CREATE TABLE pet_order (
  id               BIGINT        AUTO_INCREMENT PRIMARY KEY,
  order_no         VARCHAR(32)   NOT NULL,
  user_id          BIGINT        NOT NULL,
  pet_id           BIGINT        NOT NULL,
  merchant_id      BIGINT        NOT NULL,
  room_id          BIGINT        NOT NULL,
  start_date       DATE          NOT NULL,
  end_date         DATE          NOT NULL,
  days             INT,
  price_per_day    DECIMAL(10,2),
  total_amount     DECIMAL(10,2),
  status           VARCHAR(30)   NOT NULL,
  contact_name     VARCHAR(50),
  contact_phone    VARCHAR(20),
  remark           VARCHAR(500),
  agreement_signed TINYINT       DEFAULT 0,
  pay_time         DATETIME,
  checkin_time     DATETIME,
  checkout_time    DATETIME,
  weight_in        DECIMAL(5,2),
  weight_out       DECIMAL(5,2),
  reject_reason    VARCHAR(255),
  create_time      DATETIME      DEFAULT CURRENT_TIMESTAMP,
  update_time      DATETIME
);
CREATE INDEX idx_order_user ON pet_order (user_id);
CREATE INDEX idx_order_merchant ON pet_order (merchant_id);
CREATE INDEX idx_order_room ON pet_order (room_id);
CREATE INDEX idx_order_status ON pet_order (status);

CREATE TABLE pet_care_record (
  id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
  order_id    BIGINT        NOT NULL,
  merchant_id BIGINT        NOT NULL,
  record_date DATE          NOT NULL,
  meal_done   TINYINT       DEFAULT 0,
  walk_done   TINYINT       DEFAULT 0,
  mood        VARCHAR(20),
  content     VARCHAR(1000),
  photos      CLOB,
  weight      DECIMAL(5,2),
  create_time DATETIME      DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_care_order ON pet_care_record (order_id);

CREATE TABLE pet_review (
  id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
  order_id    BIGINT       NOT NULL,
  user_id     BIGINT       NOT NULL,
  merchant_id BIGINT       NOT NULL,
  rating      INT          NOT NULL,
  content     VARCHAR(500),
  photos      CLOB,
  reply       VARCHAR(500),
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_review_merchant ON pet_review (merchant_id);

CREATE TABLE pet_message (
  id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT       NOT NULL,
  title       VARCHAR(100),
  content     VARCHAR(500),
  type        VARCHAR(20)  DEFAULT 'SYSTEM',
  is_read     TINYINT      DEFAULT 0,
  ref_id      BIGINT,
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_msg_user ON pet_message (user_id);
