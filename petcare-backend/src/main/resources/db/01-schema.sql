-- ============================================================
-- 猫狗宠物寄养系统 · 全量建表脚本（MySQL 8）
-- 仅作用于 petcare 库；开发模式每次启动重建
-- ============================================================
SET NAMES utf8mb4;

-- 顺循依赖：先删子表
DROP TABLE IF EXISTS pet_story, user_coupon, coupon, announcement, service_booking, store_profile,
  sys_file, operation_log, sys_role_permission, sys_permission,
  review, notification, pet_exception, pet_daily_report,
  feeding_record, activity_record, medication_record, cleaning_record, pet_health, care_task,
  payment, boarding_order_service, boarding_order,
  service, room, room_type,
  pet_vaccine, pet_tag_relation, pet_tag, pet,
  employee, sys_user_role, sys_role, sys_user;

-- ---------- 系统与权限 ----------
CREATE TABLE sys_user (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  username    VARCHAR(50)  NOT NULL,
  phone       VARCHAR(20)  NULL,
  password    VARCHAR(100) NOT NULL COMMENT 'BCrypt',
  nickname    VARCHAR(50)  NULL,
  avatar      VARCHAR(255) NULL,
  security_question VARCHAR(100) NULL COMMENT '密保问题',
  security_answer   VARCHAR(100) NULL COMMENT '密保答案(BCrypt)',
  status      TINYINT      DEFAULT 1 COMMENT '1正常 0禁用',
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     NULL,
  deleted     TINYINT      DEFAULT 0,
  UNIQUE KEY uk_username (username),
  UNIQUE KEY uk_phone (phone),
  KEY idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_role (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  code        VARCHAR(30)  NOT NULL COMMENT 'ROLE_USER/ROLE_STAFF/ROLE_ADMIN',
  name        VARCHAR(30)  NOT NULL,
  permissions VARCHAR(500) COMMENT '权限码,逗号分隔',
  status      TINYINT      DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_user_role (
  id      BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_permission (
  id        BIGINT AUTO_INCREMENT PRIMARY KEY,
  code      VARCHAR(50) NOT NULL,
  name      VARCHAR(50) NOT NULL,
  type      VARCHAR(10) NOT NULL COMMENT 'MENU/BUTTON',
  parent_id BIGINT      DEFAULT 0,
  sort      INT         DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_role_permission (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_id       BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  UNIQUE KEY uk_role_perm (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- 宠物域 ----------
CREATE TABLE pet (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT       NOT NULL,
  name        VARCHAR(50)  NOT NULL,
  species     VARCHAR(10)  NOT NULL COMMENT 'CAT/DOG',
  breed       VARCHAR(50)  NULL,
  gender      VARCHAR(10)  NULL COMMENT 'M/F',
  birth_date  DATE         NULL,
  weight      DECIMAL(5,2) NULL COMMENT '当前体重kg',
  sterilized  TINYINT      DEFAULT 0,
  fur_color   VARCHAR(30)  NULL,
  photo       VARCHAR(255) NULL,
  temperament VARCHAR(200) NULL COMMENT '性格描述',
  notes       VARCHAR(500) NULL COMMENT '注意事项（员工端置顶）',
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     NULL,
  deleted     TINYINT      DEFAULT 0,
  KEY idx_pet_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pet_tag (
  id     BIGINT AUTO_INCREMENT PRIMARY KEY,
  name   VARCHAR(20) NOT NULL,
  status TINYINT     DEFAULT 1,
  UNIQUE KEY uk_tag_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pet_tag_relation (
  id      BIGINT AUTO_INCREMENT PRIMARY KEY,
  pet_id  BIGINT NOT NULL,
  tag_id  BIGINT NOT NULL,
  UNIQUE KEY uk_pet_tag (pet_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pet_vaccine (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  pet_id            BIGINT      NOT NULL,
  vaccine_name      VARCHAR(50) NOT NULL,
  inoculation_date  DATE        NOT NULL,
  expire_date       DATE        NULL,
  org               VARCHAR(100) NULL,
  remark            VARCHAR(255) NULL,
  create_time       DATETIME    DEFAULT CURRENT_TIMESTAMP,
  update_time       DATETIME    NULL,
  deleted           TINYINT     DEFAULT 0,
  KEY idx_vaccine_pet (pet_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pet_health (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  pet_id        BIGINT      NOT NULL,
  order_id      BIGINT      NULL COMMENT '关联寄养订单，日常记录为空',
  record_date   DATE        NOT NULL,
  weight        DECIMAL(5,2) NULL,
  temperature   DECIMAL(4,1) NULL,
  mental_state  VARCHAR(20) NULL,
  appetite      VARCHAR(20) NULL,
  water         VARCHAR(20) NULL,
  poop          VARCHAR(20) NULL,
  is_abnormal   TINYINT     DEFAULT 0,
  remark        VARCHAR(255) NULL,
  staff_id      BIGINT      NULL,
  create_time   DATETIME    DEFAULT CURRENT_TIMESTAMP,
  KEY idx_health_pet (pet_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- 房间域 ----------
CREATE TABLE room_type (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(50)   NOT NULL,
  species     VARCHAR(10)   NOT NULL DEFAULT 'DOG' COMMENT '分区：DOG犬区/CAT猫区',
  price       DECIMAL(10,2) NOT NULL,
  area        DECIMAL(6,2)  NULL COMMENT '面积㎡',
  max_pets    INT           DEFAULT 1,
  facilities  VARCHAR(255)  NULL COMMENT '设施,逗号分隔',
  description VARCHAR(500)  NULL,
  photo       VARCHAR(255)  NULL,
  status      TINYINT       DEFAULT 1 COMMENT '1上架 0下架',
  sort        INT           DEFAULT 0,
  create_time DATETIME      DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME      NULL,
  deleted     TINYINT       DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE room (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  room_no          VARCHAR(10)  NOT NULL,
  floor            INT          NOT NULL,
  room_type_id     BIGINT       NOT NULL,
  status           VARCHAR(15)  DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE/RESERVED/OCCUPIED/CLEANING/MAINTENANCE',
  current_order_id BIGINT       NULL,
  note             VARCHAR(255) NULL,
  create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP,
  update_time      DATETIME     NULL,
  deleted          TINYINT      DEFAULT 0,
  UNIQUE KEY uk_room_no (room_no),
  KEY idx_room_status (status),
  KEY idx_room_type (room_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- 服务域 ----------
CREATE TABLE service (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(50)   NOT NULL,
  photo       VARCHAR(255)  NULL,
  price       DECIMAL(10,2) NOT NULL,
  unit        VARCHAR(20)   DEFAULT '次',
  description VARCHAR(255)  NULL,
  status      TINYINT       DEFAULT 1,
  sort        INT           DEFAULT 0,
  create_time DATETIME      DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME      NULL,
  deleted     TINYINT       DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- 寄养订单域 ----------
CREATE TABLE boarding_order (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_no       VARCHAR(32)   NOT NULL,
  user_id        BIGINT        NOT NULL,
  pet_id         BIGINT        NOT NULL,
  room_type_id   BIGINT        NOT NULL,
  room_id        BIGINT        NULL COMMENT '入住时分配',
  start_date     DATE          NOT NULL,
  end_date       DATE          NOT NULL,
  days           INT           NOT NULL,
  room_amount    DECIMAL(10,2) NOT NULL,
  service_amount DECIMAL(10,2) DEFAULT 0,
  discount_amount DECIMAL(10,2) DEFAULT 0 COMMENT '优惠券抵扣',
  total_amount   DECIMAL(10,2) NOT NULL,
  status         VARCHAR(20)   NOT NULL COMMENT 'PENDING_PAYMENT/PAID/CONFIRMED/CHECKED_IN/BOARDING/COMPLETED/CANCELLED/REFUNDING/REFUNDED',
  contact_name   VARCHAR(50)   NULL,
  contact_phone  VARCHAR(20)   NULL,
  remark         VARCHAR(500)  NULL,
  pay_time       DATETIME      NULL,
  checkin_time   DATETIME      NULL,
  checkout_time  DATETIME      NULL,
  cancel_reason  VARCHAR(255)  NULL,
  create_time    DATETIME      DEFAULT CURRENT_TIMESTAMP,
  update_time    DATETIME      NULL,
  deleted        TINYINT       DEFAULT 0,
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_order_user (user_id),
  KEY idx_order_status_dates (status, start_date, end_date),
  KEY idx_order_room (room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE boarding_order_service (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id     BIGINT        NOT NULL,
  service_id   BIGINT        NOT NULL,
  service_name VARCHAR(50)   NOT NULL COMMENT '冗余，防改价影响历史',
  price        DECIMAL(10,2) NOT NULL,
  unit         VARCHAR(20)   NULL,
  quantity     INT           DEFAULT 1,
  amount       DECIMAL(10,2) NOT NULL,
  KEY idx_bos_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE payment (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  payment_no VARCHAR(32)   NOT NULL,
  order_id   BIGINT        NOT NULL,
  user_id    BIGINT        NOT NULL,
  amount     DECIMAL(10,2) NOT NULL,
  status     VARCHAR(20)   NOT NULL COMMENT 'CREATED/SUCCESS/FAILED/CANCELLED/REFUNDING/REFUNDED',
  channel    VARCHAR(20)   DEFAULT 'SIMULATED',
  pay_time   DATETIME      NULL,
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_payment_no (payment_no),
  KEY idx_payment_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- 照护域 ----------
CREATE TABLE care_task (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id   BIGINT      NOT NULL,
  pet_id     BIGINT      NOT NULL,
  room_id    BIGINT      NULL,
  task_type  VARCHAR(20) NOT NULL COMMENT 'FEEDING/WATER/WALK/CLEANING/MEDICATION/PHOTO/HEALTH_CHECK/CHECKIN_CHECK/CHECKOUT_CHECK',
  task_date  DATE        NOT NULL,
  plan_time  VARCHAR(5)  NULL COMMENT '计划时间点 08:30',
  title      VARCHAR(50) NOT NULL,
  content    VARCHAR(200) NULL COMMENT '如 狗粮150g',
  status     VARCHAR(10) DEFAULT 'PENDING' COMMENT 'PENDING/DONE/SKIPPED',
  staff_id   BIGINT      NULL,
  done_time  DATETIME    NULL,
  remark     VARCHAR(255) NULL,
  create_time DATETIME   DEFAULT CURRENT_TIMESTAMP,
  KEY idx_task_date (task_date, status),
  KEY idx_task_order (order_id),
  KEY idx_task_staff (staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE feeding_record (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  task_id     BIGINT      NOT NULL,
  order_id    BIGINT      NOT NULL,
  pet_id      BIGINT      NOT NULL,
  food        VARCHAR(50) NULL,
  amount      VARCHAR(30) NULL,
  appetite    VARCHAR(20) NULL,
  staff_id    BIGINT      NULL,
  record_time DATETIME    DEFAULT CURRENT_TIMESTAMP,
  remark      VARCHAR(255) NULL,
  KEY idx_feeding_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE activity_record (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  task_id          BIGINT      NOT NULL,
  order_id         BIGINT      NOT NULL,
  pet_id           BIGINT      NOT NULL,
  duration_minutes INT         NULL,
  mood             VARCHAR(20) NULL,
  photos           VARCHAR(500) NULL,
  staff_id         BIGINT      NULL,
  record_time      DATETIME    DEFAULT CURRENT_TIMESTAMP,
  remark           VARCHAR(255) NULL,
  KEY idx_activity_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE medication_record (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  task_id     BIGINT      NOT NULL,
  order_id    BIGINT      NOT NULL,
  pet_id      BIGINT      NOT NULL,
  medicine    VARCHAR(50) NULL,
  dosage      VARCHAR(50) NULL,
  staff_id    BIGINT      NULL,
  record_time DATETIME    DEFAULT CURRENT_TIMESTAMP,
  remark      VARCHAR(255) NULL,
  KEY idx_med_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE cleaning_record (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  task_id     BIGINT   NOT NULL,
  room_id     BIGINT   NOT NULL,
  order_id    BIGINT   NULL,
  staff_id    BIGINT   NULL,
  record_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_cleaning_room (room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- 日报 / 异常 / 通知 / 评价 ----------
CREATE TABLE pet_daily_report (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id         BIGINT      NOT NULL,
  pet_id           BIGINT      NOT NULL,
  report_date      DATE        NOT NULL,
  mental_state     VARCHAR(20) NULL,
  appetite         VARCHAR(20) NULL,
  water            VARCHAR(20) NULL,
  poop             VARCHAR(20) NULL,
  is_abnormal      TINYINT     DEFAULT 0,
  activity_minutes INT         DEFAULT 0,
  meals_summary    VARCHAR(500) NULL,
  staff_note       VARCHAR(500) NULL,
  photos           VARCHAR(500) NULL,
  staff_id         BIGINT      NULL,
  create_time      DATETIME    DEFAULT CURRENT_TIMESTAMP,
  update_time      DATETIME    NULL,
  UNIQUE KEY uk_report (order_id, report_date),
  KEY idx_report_pet (pet_id, report_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pet_exception (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  pet_id          BIGINT       NOT NULL,
  order_id        BIGINT       NULL,
  type            VARCHAR(20)  NOT NULL COMMENT 'MENTAL/APPETITE/VOMIT/DIARRHEA/INJURY/BEHAVIOR/OTHER',
  level           VARCHAR(10)  NOT NULL COMMENT 'MILD/NORMAL/SEVERE',
  description     VARCHAR(500) NOT NULL,
  photos          VARCHAR(500) NULL,
  status          VARCHAR(15)  DEFAULT 'PENDING' COMMENT 'PENDING/PROCESSING/OBSERVING/RESOLVED/CLOSED',
  report_staff_id BIGINT       NOT NULL,
  handler_id      BIGINT       NULL,
  handle_note     VARCHAR(500) NULL,
  handle_time     DATETIME     NULL,
  create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  update_time     DATETIME     NULL,
  KEY idx_exception_pet (pet_id),
  KEY idx_exception_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notification (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT       NOT NULL,
  title       VARCHAR(100) NOT NULL,
  content     VARCHAR(500) NULL,
  type        VARCHAR(20)  DEFAULT 'SYSTEM' COMMENT 'DAILY/REMINDER/EXCEPTION/SYSTEM',
  is_read     TINYINT      DEFAULT 0,
  ref_type    VARCHAR(20)  NULL,
  ref_id      BIGINT       NULL,
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
  KEY idx_notify_user (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE review (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id           BIGINT     NOT NULL,
  user_id            BIGINT     NOT NULL,
  pet_id             BIGINT     NULL,
  rating_overall     TINYINT    NOT NULL,
  rating_env         TINYINT    NULL,
  rating_service     TINYINT    NULL,
  rating_staff       TINYINT    NULL,
  rating_pet_status  TINYINT    NULL,
  content            VARCHAR(500) NULL,
  tags               VARCHAR(255) NULL,
  reply              VARCHAR(500) NULL,
  reply_time         DATETIME   NULL,
  create_time        DATETIME   DEFAULT CURRENT_TIMESTAMP,
  update_time        DATETIME   NULL,
  UNIQUE KEY uk_review_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- 支撑域 ----------
CREATE TABLE employee (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT      NOT NULL,
  emp_no      VARCHAR(20) NOT NULL,
  real_name   VARCHAR(50) NOT NULL,
  avatar      VARCHAR(255) NULL,
  phone       VARCHAR(20) NULL,
  position    VARCHAR(30) NULL COMMENT '驯养师/护理师/前台',
  area        VARCHAR(30) NULL COMMENT '所属区域',
  status      TINYINT     DEFAULT 1,
  hire_date   DATE        NULL,
  create_time DATETIME    DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_emp_user (user_id),
  UNIQUE KEY uk_emp_no (emp_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE operation_log (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT       NULL,
  username    VARCHAR(50)  NULL,
  module      VARCHAR(30)  NULL,
  action      VARCHAR(30)  NULL,
  detail      VARCHAR(500) NULL,
  ip          VARCHAR(50)  NULL,
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
  KEY idx_log_user (user_id),
  KEY idx_log_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_file (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  storage_name  VARCHAR(100) NOT NULL,
  original_name VARCHAR(200) NOT NULL,
  path          VARCHAR(300) NOT NULL,
  biz_type      VARCHAR(20)  NULL,
  size          BIGINT       NULL,
  content_type  VARCHAR(100) NULL,
  upload_by     BIGINT       NULL,
  create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_file_name (storage_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ---------- 门店信息 ----------
CREATE TABLE store_profile (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  store_name  VARCHAR(80)  NOT NULL,
  slogan      VARCHAR(160) NULL,
  intro       VARCHAR(1000) NULL,
  address     VARCHAR(200) NULL,
  phone       VARCHAR(20)  NULL,
  open_hours  VARCHAR(80)  NULL,
  features    VARCHAR(500) NULL COMMENT '设施特色，逗号分隔',
  photos      VARCHAR(1000) NULL COMMENT '门店照片URL，逗号分隔',
  update_time DATETIME     NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- 服务预约（美容/洗澡/寄养接送，独立于寄养主订单） ----------
CREATE TABLE service_booking (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT        NOT NULL,
  pet_id      BIGINT        NOT NULL,
  service_id  BIGINT        NOT NULL,
  service_name VARCHAR(50)  NOT NULL,
  price       DECIMAL(10,2) NOT NULL,
  book_date   DATE          NOT NULL,
  book_time   VARCHAR(10)   NULL COMMENT '如 10:00',
  status      VARCHAR(20)   DEFAULT 'PENDING' COMMENT 'PENDING/CONFIRMED/DONE/CANCELLED',
  remark      VARCHAR(255)  NULL,
  create_time DATETIME      DEFAULT CURRENT_TIMESTAMP,
  KEY idx_booking_user (user_id),
  KEY idx_booking_date (book_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- 优惠券 ----------
CREATE TABLE coupon (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  name         VARCHAR(60)  NOT NULL,
  type         VARCHAR(20)  NOT NULL COMMENT 'DISCOUNT满减/PERCENT折扣',
  threshold    DECIMAL(10,2) DEFAULT 0 COMMENT '满减门槛',
  amount       DECIMAL(10,2) DEFAULT 0 COMMENT '减免金额',
  percent      INT           DEFAULT 0 COMMENT '折扣如90=9折',
  total_count  INT           DEFAULT 0,
  issued_count INT           DEFAULT 0,
  start_date   DATE          NULL,
  end_date     DATE          NULL,
  status       TINYINT       DEFAULT 1,
  create_time  DATETIME      DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_coupon (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  coupon_id   BIGINT        NOT NULL,
  user_id     BIGINT        NOT NULL,
  status      VARCHAR(20)   DEFAULT 'UNUSED' COMMENT 'UNUSED/USED/EXPIRED',
  used_order_id BIGINT      NULL,
  get_time    DATETIME      DEFAULT CURRENT_TIMESTAMP,
  use_time    DATETIME      NULL,
  UNIQUE KEY uk_user_coupon (user_id, coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- 平台公告 ----------
CREATE TABLE announcement (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  title       VARCHAR(100) NOT NULL,
  content     VARCHAR(1000) NULL,
  type        VARCHAR(20)  DEFAULT 'NOTICE' COMMENT 'NOTICE/ACTIVITY/SAFETY',
  pinned      TINYINT      DEFAULT 0,
  status      TINYINT      DEFAULT 1,
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- 主人分享故事墙 ----------
CREATE TABLE pet_story (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT       NOT NULL,
  pet_id      BIGINT       NULL,
  title       VARCHAR(100) NOT NULL,
  content     VARCHAR(1500) NULL,
  photos      VARCHAR(1000) NULL,
  likes       INT          DEFAULT 0,
  status      TINYINT      DEFAULT 1,
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
  KEY idx_story_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
