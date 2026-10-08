# DATABASE_DESIGN — 猫狗宠物寄养系统 数据库设计（MySQL 8）

> 数据库名：`petcare`（utf8mb4）。所有主表统一基础字段：`id BIGINT AUTO_INCREMENT`、`create_time`、`update_time`、`deleted TINYINT DEFAULT 0`（逻辑删除，业务表适用）。
> 命名：小写下划线；状态用 VARCHAR 存枚举名，便于阅读与排查。

## 1. 系统与权限（5 表）

**sys_user** 用户
| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | |
| username | VARCHAR(50) UK | 登录名 |
| phone | VARCHAR(20) UK | |
| password | VARCHAR(100) | BCrypt 散列 |
| nickname | VARCHAR(50) | 昵称/真实姓名 |
| avatar | VARCHAR(255) | 头像 URL |
| status | TINYINT | 1 正常 0 禁用 |
| create_time / update_time / deleted | | 通用 |

**sys_role** 角色：`id, code(ROLE_USER/ROLE_STAFF/ROLE_ADMIN), name, permissions(菜单/按钮码,逗号分隔), status`
**sys_user_role**：`id, user_id, role_id`（UK: user_id+role_id）
**sys_permission**（预留）：`id, code, name, type(MENU/BUTTON), parent_id, sort`
**sys_role_permission**（预留）：`role_id, permission_id`

## 2. 宠物域（6 表）

**pet** 宠物档案
| 字段 | 类型 | 说明 |
|---|---|---|
| id / user_id | BIGINT | 主人 |
| name | VARCHAR(50) | 昵称 |
| species | VARCHAR(10) | CAT / DOG |
| breed | VARCHAR(50) | 品种 |
| gender | VARCHAR(10) | M / F |
| birth_date | DATE | 生日（年龄由服务端计算） |
| weight | DECIMAL(5,2) | 当前体重 kg（离店时更新） |
| sterilized | TINYINT | 绝育 |
| fur_color | VARCHAR(30) | 毛色 |
| photo | VARCHAR(255) | 档案照 |
| temperament | VARCHAR(200) | 性格描述 |
| notes | VARCHAR(500) | 注意事项（员工端置顶展示） |
| create_time / update_time / deleted | | |

**pet_tag** 标签字典：`id, name UK, status`　（#亲人 #怕打雷 #喜欢球…）
**pet_tag_relation**：`id, pet_id, tag_id`（UK: pet_id+tag_id）

**pet_vaccine** 疫苗记录
`id, pet_id, vaccine_name, inoculation_date DATE, expire_date DATE, org, remark, create_time`
状态（有效/即将到期/已过期）由 `expire_date` 服务端计算，不落库。

**pet_health** 健康观察记录（非医疗，仅观察）
`id, pet_id, order_id NULL, record_date DATE, weight DECIMAL(5,2), temperature DECIMAL(4,1), mental_state VARCHAR(20), appetite VARCHAR(20), water VARCHAR(20), poop VARCHAR(20), is_abnormal TINYINT, remark, staff_id, create_time`
索引：`(pet_id, record_date)`

## 3. 房间域（2 表）

**room_type** 房型
`id, name(标准房/舒适房/豪华房/VIP宠物套房), price DECIMAL(10,2), area DECIMAL(6,2), max_pets INT, facilities VARCHAR(255)(逗号), description, photo, status TINYINT(1上架0下架), sort INT`

**room** 房间（物理单间）
`id, room_no VARCHAR(10) UK(101…110,201…210), floor INT, room_type_id, status VARCHAR(15)(AVAILABLE/RESERVED/OCCUPIED/CLEANING/MAINTENANCE), current_order_id BIGINT NULL, note`
索引：`(status)`, `(room_type_id)`
状态机：AVAILABLE → RESERVED(有未入住订单) → OCCUPIED → CLEANING → AVAILABLE；MAINTENANCE 任意可维护状态下手工进入/退出。

## 4. 服务域（1 表）

**service** 增值服务
`id, name(洗澡/美容/单独遛宠/宠物摄影/接送/喂药/特殊饮食), photo, price DECIMAL(10,2), unit(次/30分钟/单程), description, status TINYINT, sort INT`

## 5. 寄养订单域（3 表）

**boarding_order** 寄养订单
| 字段 | 类型 | 说明 |
|---|---|---|
| id / order_no UK | | PS + 时间戳 |
| user_id / pet_id | BIGINT | |
| room_type_id / room_id | BIGINT | room_id 入住时分配 |
| start_date / end_date | DATE | 入住 < 离店 |
| days | INT | 服务端计算 |
| room_amount / service_amount / total_amount | DECIMAL(10,2) | 服务端计算，禁止前端传入 |
| status | VARCHAR(20) | 见状态机 |
| contact_name / contact_phone / remark | | |
| pay_time / checkin_time / checkout_time | DATETIME | |
| cancel_reason | VARCHAR(255) | |
| create_time / update_time / deleted | | |

状态机（后端唯一裁决，禁止前端改状态）：
`PENDING_PAYMENT → PAID → CONFIRMED → CHECKED_IN → BOARDING → COMPLETED`
旁路：`PENDING_PAYMENT/PAID → CANCELLED`（未入住才可取消）；`PAID/CONFIRMED → REFUNDING → REFUNDED`。
并发控制：确认订单占房 `UPDATE room SET status='OCCUPIED', current_order_id=? WHERE id=? AND status IN ('AVAILABLE','CLEANING')`，受影响行数=0 则失败重试/报错；创建订单校验库存用区间重叠计数 SQL + 事务。

**boarding_order_service** 订单服务明细
`id, order_id, service_id, service_name, price, unit, quantity INT, amount`（冗余名称与单价，防服务改价影响历史订单）

**payment** 支付流水（模拟支付，保留真实模型）
`id, payment_no UK, order_id, user_id, amount, status(CREATED/SUCCESS/FAILED/CANCELLED/REFUNDING/REFUNDED), channel VARCHAR(20) DEFAULT 'SIMULATED', pay_time, create_time`

## 6. 照护域（6 表）

**care_task** 照护任务（入住确认后由系统按订单日期批量生成）
`id, order_id, pet_id, room_id, task_type(FEEDING/WATER/WALK/CLEANING/MEDICATION/PHOTO/HEALTH_CHECK/CHECKIN_CHECK/CHECKOUT_CHECK), task_date DATE, plan_time VARCHAR(5)(08:30…), title, content, status(PENDING/DONE/SKIPPED), staff_id NULL, done_time, remark`
索引：`(task_date, status)`, `(order_id)`

完成任务时按类型落对应记录表：

**feeding_record**：`id, task_id, order_id, pet_id, food VARCHAR(50), amount VARCHAR(30), appetite VARCHAR(20), staff_id, record_time, remark`
**activity_record**：`id, task_id, order_id, pet_id, duration_minutes INT, mood VARCHAR(20), photos VARCHAR(500), staff_id, record_time, remark`
**medication_record**：`id, task_id, order_id, pet_id, medicine, dosage, staff_id, record_time, remark`
**cleaning_record**：`id, task_id, room_id, order_id NULL, staff_id, record_time`
**pet_health**（复用第 2 节）

## 7. 日报 / 异常 / 通知 / 评价（4 表）

**pet_daily_report** 宠物每日照护日报（核心创新，每订单每宠物每天一条）
`id, order_id, pet_id, report_date DATE UK(order_id+report_date), mental_state, appetite, water, poop, is_abnormal TINYINT, activity_minutes INT DEFAULT 0, meals_summary VARCHAR(500)(由当天喂食记录汇总), staff_note VARCHAR(500), photos VARCHAR(500), staff_id, create_time`

**pet_exception** 异常事件（生命周期：PENDING → PROCESSING → OBSERVING → RESOLVED → CLOSED）
`id, pet_id, order_id, type(MENTAL/APPETITE/VOMIT/DIARRHEA/INJURY/BEHAVIOR/OTHER), level(MILD/NORMAL/SEVERE), description VARCHAR(500), photos, status, report_staff_id, handler_id NULL, handle_note VARCHAR(500) NULL, handle_time NULL, create_time, update_time`
规则：NORMAL/SEVERE 自动通知管理员与相关员工；SEVERE 额外通知主人。

**notification** 通知
`id, user_id, title, content, type(DAILY/REMINDER/EXCEPTION/SYSTEM), is_read TINYINT, ref_type VARCHAR(20) NULL, ref_id BIGINT NULL, create_time`　索引：`(user_id, is_read)`

**review** 评价
`id, order_id UK, user_id, pet_id, rating_overall/rating_env/rating_service/rating_staff/rating_pet_status TINYINT(1-5), content VARCHAR(500), tags VARCHAR(255)(逗号), reply VARCHAR(500) NULL, reply_time NULL, create_time`

## 8. 支撑域（3 表）

**employee** 员工档案：`id, user_id UK, emp_no UK, real_name, avatar, phone, position(驯养师/护理师/前台), area, status TINYINT, hire_date`
**operation_log** 操作日志：`id, user_id, username, module, action, detail VARCHAR(500), ip, create_time`（登录/退出/下单/取消/入住/离店/档案修改/异常处理/房间与服务修改/管理操作）
**sys_file** 文件：`id, storage_name UK, original_name, path, biz_type(PET/ROOM/SERVICE/EXCEPTION/AVATAR), size BIGINT, content_type, upload_by, create_time`

## 9. 表关系总览

```
sys_user ─┬─ sys_user_role ─ sys_role
          ├─ pet ─┬─ pet_tag_relation ─ pet_tag
          │       ├─ pet_vaccine
          │       ├─ pet_health
          │       ├─ pet_exception ─→ notification
          │       └─ pet_daily_report
          └─ boarding_order ─┬─ boarding_order_service ─ service
                             ├─ payment
                             ├─ care_task ─ feeding/activity/medication/cleaning/health_record
                             ├─ room ─ room_type
                             └─ review
employee ─ sys_user；operation_log / sys_file 独立
```

## 10. 数据规模与索引策略

- 高频查询索引：`care_task(task_date,status)`、`boarding_order(user_id)`、`boarding_order(status,start_date,end_date)`（库存重叠计算）、`notification(user_id,is_read)`、`pet_health(pet_id,record_date)`；`sys_user` 双唯一键 `uk_username` / `uk_phone`（并发注册兜底）
- 演示数据量：用户 8、宠物 12、房型 8（犬区 4 + 猫区 4）、房间 60（3 层 × 20 间）、订单 30+（覆盖全状态）、任务 200+
- 初始化 SQL 拆分：`01-schema.sql` / `02-seed.sql`；默认 `sql.init.mode=never` 持久化，`demo` profile 启动时全量重建

## 11. 扩展模块表补遗

后续迭代新增（门店/营销/故事墙）共 7 张表，见 `01-schema.sql`：

| 表 | 用途 | 关键字段 |
|---|---|---|
| `store_profile` | 单店信息（店名/口号/地址/营业时间/设施/介绍/照片） | 单行 |
| `service_booking` | 门店服务预约（洗护美容等） | service_id/user_id/status(PENDING/CONFIRMED/DONE/CANCELLED)/book_time |
| `coupon` | 优惠券模板 | type(DISCOUNT满减/PERCENT折扣)/threshold/amount/percent(80=8折)/total_count/issued_count/起止日期 |
| `user_coupon` | 用户已领券 | user_id/coupon_id 唯一键/status(UNUSED/USED/EXPIRED)/used_order_id |
| `announcement` | 平台公告 | type(NOTICE/ACTIVITY/SAFETY)/pinned/status |
| `pet_story` | 故事墙 | user_id/content/photo/status(审核)/likes |
| `sys_user`（扩展列） | 密保问题 | security_question / security_answer(BCrypt) |
