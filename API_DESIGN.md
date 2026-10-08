# API_DESIGN — 猫狗宠物寄养系统 接口设计

> 基础约定：统一前缀 `/api`；统一返回 `{code:200, message:"success", data:{}, timestamp}`；code=200 成功、400 参数/业务错误、401 未登录、403 无权限、500 系统错误。
> 认证：`Authorization: Bearer <JWT>`（Spring Security 无状态过滤器链）。角色：USER / STAFF / ADMIN。
> 状态修改类接口一律 `POST /xxx/{id}/动词`，由后端校验状态机合法性。

## 1. 认证与个人中心 `/api/auth`

| 方法 | 路径 | 角色 | 说明 |
|---|---|---|---|
| POST | /api/auth/captcha | 公开 | 发送图形/短信验证码（模拟），存 Redis，5 分钟有效 |
| POST | /api/auth/register | 公开 | 注册（三步注册最终提交）：username/phone/password/captcha |
| POST | /api/auth/login | 公开 | 登录，返回 token + 用户信息 + 角色 |
| POST | /api/auth/logout | 登录 | 退出，Redis 拉黑 token 剩余有效期 |
| GET | /api/auth/me | 登录 | 当前用户信息（含角色、员工档案若为 STAFF） |
| PUT | /api/auth/profile | 登录 | 修改昵称/头像 |
| PUT | /api/auth/password | 登录 | 修改密码（旧密码校验） |

业务规则：登录失败 5 次锁定 10 分钟（Redis 计数）；密码 BCrypt。

## 2. 宠物域 `/api/pets`

| 方法 | 路径 | 角色 | 说明 |
|---|---|---|---|
| GET | /api/pets?keyword | USER(本人)/ADMIN(全部) | 宠物列表（卡片数据） |
| POST / PUT / DELETE | /api/pets, /api/pets/{id} | USER | 增改删（删为逻辑删除，有寄养历史不可删） |
| GET | /api/pets/{id} | 属主/STAFF/ADMIN | 档案详情（含标签、疫苗、注意事项） |
| GET | /api/pets/{id}/vaccines · POST · PUT · DELETE | 属主 | 疫苗记录管理（状态服务端按 expire_date 计算） |
| GET | /api/pets/{id}/health?days=30 | 属主/STAFF/ADMIN | 健康观察记录 |
| PUT | /api/pets/{id}/tags | 属主 | 设置性格标签（从字典选择） |
| GET | /api/pets/{id}/growth | 属主 | 成长档案：累计寄养次数/天数、体重曲线数据 |
| GET | /api/pets/{id}/boardings | 属主 | 寄养历史列表 |
| GET | /api/pet-tags | 登录 | 标签字典 |

## 3. 房间域 `/api/rooms`、`/api/room-types`

| 方法 | 路径 | 角色 | 说明 |
|---|---|---|---|
| GET | /api/rooms/map | STAFF/ADMIN | 房间地图数据（按楼层分组 + 状态 + 当前宠物摘要） |
| GET | /api/rooms?status=&floor= | STAFF/ADMIN | 房间列表 |
| POST / PUT / DELETE | /api/rooms, /api/rooms/{id} | ADMIN | 房间 CRUD（OCCUPIED 房间不可删） |
| PUT | /api/rooms/{id}/status | STAFF/ADMIN | 手工改状态（如清洁完成、进维修） |
| GET | /api/room-types | 公开 | 房型列表（预订选择用） |
| POST / PUT / DELETE | /api/room-types… | ADMIN | 房型 CRUD |

## 4. 增值服务 `/api/services`

GET /api/services（公开，仅上架）；ADMIN 增删改 + 排序/启停。

## 5. 预订与订单 `/api/boarding`、`/api/orders`

| 方法 | 路径 | 角色 | 说明 |
|---|---|---|---|
| POST | /api/boarding/availability | USER | body: {petId, startDate, endDate} → 各房型每日剩余/可订状态（联动日历 🟢🟡🔴） |
| POST | /api/boarding/orders | USER | 创建订单：petId/roomTypeId/dates/services[]/contact…；事务内算价+查库存；返回订单 |
| GET | /api/orders?status=&page= | USER(本人)/STAFF/ADMIN | 订单列表（角色自适应数据范围） |
| GET | /api/orders/{id} | 属主/STAFF/ADMIN | 订单详情（含服务明细、支付流水、宠物、房间） |
| POST | /api/orders/{id}/pay | USER | 模拟支付：写 payment SUCCESS，订单 → PAID |
| POST | /api/orders/{id}/cancel | USER | 仅 PENDING_PAYMENT/PAID 可取消，写原因 |
| POST | /api/orders/{id}/refund | USER | PAID/CONFIRMED → REFUNDING；ADMIN 处理 → REFUNDED |
| POST | /api/orders/{id}/confirm | ADMIN | PAID → CONFIRMED，预分配房间（RESERVED），生成照护任务骨架 |
| POST | /api/orders/{id}/check-in | STAFF | CONFIRMED → CHECKED_IN：入住检查任务完成，房间 OCCUPIED，绑定 room_id |
| POST | /api/orders/{id}/start-boarding | STAFF | CHECKED_IN → BOARDING（入住检查通过后开始计天） |
| POST | /api/orders/{id}/check-out | STAFF | BOARDING → COMPLETED：离店检查，更新宠物体重，房间 → CLEANING |
| POST | /api/orders/{id}/refund-approve | ADMIN | REFUNDING → REFUNDED |

业务规则：过去日期不可订；`start < end`；库存不足报错；状态机外流转一律 400。

## 6. 照护域 `/api/care`

| 方法 | 路径 | 角色 | 说明 |
|---|---|---|---|
| GET | /api/care/workbench | STAFF | 今日工作台：任务统计（今日/已完成/异常数）+ 今日任务分组列表 |
| GET | /api/care/tasks?date=&type=&status= | STAFF/ADMIN | 任务列表 |
| POST | /api/care/tasks/{id}/complete | STAFF | 完成任务：body 按类型携带（喂食 food/amount/appetize；活动 duration/mood/photos；健康 weight/temperature/…），写对应记录表 + 更新任务 DONE |
| POST | /api/care/tasks/{id}/skip | STAFF | 跳过任务（需备注） |
| GET | /api/care/pets/{orderId}/notes | STAFF | 宠物注意事项/标签（照护前必看） |

业务规则：完成任务记录 操作员工/时间/类型；任务完成数据自动汇入当日日报。

## 7. 日报 / 异常 / 通知

| 方法 | 路径 | 角色 | 说明 |
|---|---|---|---|
| GET | /api/orders/{id}/daily-reports | 属主/STAFF/ADMIN | 日报列表（时间线数据源） |
| GET | /api/orders/{id}/daily-reports/{date} | 同上 | 单日日报详情 |
| GET | /api/pets/{id}/daily-reports?days=30 | 属主 | 跨订单日报（成长档案） |
| GET | /api/exceptions?status=&level= | STAFF/ADMIN | 异常列表 |
| POST | /api/exceptions | STAFF | 上报异常（→ 通知规则见库表设计） |
| POST | /api/exceptions/{id}/process \| observe \| resolve \| close | ADMIN（close 亦可 STAFF 确认） | 生命周期流转，写 handler/handle_note |
| GET | /api/notifications?unreadOnly= | 登录 | 通知列表（Redis 缓存未读数） |
| GET | /api/notifications/unread-count | 登录 | 未读数 |
| POST | /api/notifications/{id}/read · /read-all | 登录 | 已读 |

## 8. 评价 `/api/reviews`

POST /api/orders/{id}/review（USER，仅 COMPLETED 且未评价；五维评分 + 标签）；GET /api/reviews（ADMIN，管理视图）；POST /api/reviews/{id}/reply（ADMIN）。

## 9. 管理端 `/api/admin`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/admin/dashboard | 驾驶舱：今日营业额/当前入住/今日入住/今日离店/房间使用率/宠物状态分布（Redis 缓存 60s） |
| GET | /api/admin/stats/trend?days=7\|30 | 营业额/订单量/入住数趋势 |
| GET | /api/admin/stats/services | 热门服务横向柱状图数据 |
| GET | /api/admin/stats/staff | 员工任务完成率 |
| GET/PUT | /api/admin/employees… | 员工管理（含任务完成统计） |
| GET | /api/admin/users · PUT /{id}/status | 用户管理（禁用/启用） |
| GET | /api/admin/pets?keyword | 宠物管理（全店视角） |
| GET | /api/admin/logs | 操作日志 |

## 10. 文件 `/api/files`

POST /api/files/upload（登录；multipart，biz_type 校验类型与 5MB 上限，本地存储 `./upload/yyyy/MM/uuid.ext`）；GET /upload/** 静态映射；返回可访问 URL。

## 11. 分页约定

列表接口返回 `data: {records, total, current, size}`（MyBatis-Plus Page 直出）。

## 12. 扩展模块接口补遗（store / marketing / story）

**门店服务预约**（`store` 模块）
- `GET /api/services`：增值服务列表（登录可看，含下架过滤）
- `GET /api/store/profile` / `PUT /api/store/profile`（ADMIN）：门店信息
- `POST /api/service-bookings`：主人预约门店服务
- `GET /api/service-bookings/mine`：我的预约
- `GET /api/service-bookings?date=`（STAFF/ADMIN）：按日查看门店预约
- `POST /api/service-bookings/{id}/{action}`：action=CONFIRM | DONE（员工确认/完成）

**营销优惠券**（`marketing` 模块；type：`DISCOUNT`=满减 threshold/amount，`PERCENT`=折扣 percent=折数×10）
- `GET /api/coupons/available`：可领券列表（已过滤 percent≥100 的无效券）
- `POST /api/coupons/{id}/claim`：领券（防重复/限量/过期校验）
- `GET /api/coupons/mine`：我的券（返回 UserCoupon+券模板 VO，未使用的过期券惰性置 EXPIRED）
- `POST /api/orders/{id}/apply-coupon`：下单自动应用最优券
- `GET/POST/DELETE /api/admin/coupons`（ADMIN + `@RequirePermission("marketing:coupon")`）
- `GET /api/announcements`；`POST/DELETE /api/admin/announcements`（ADMIN + `@RequirePermission("marketing:announcement")`）

**故事墙**（`story` 模块）
- `POST /api/stories`：发布故事；`POST /api/stories/{id}/like`：点赞
- `GET /api/stories`：已过审故事
- `GET /api/admin/stories`、`PUT /api/admin/stories/{id}/status`（ADMIN + `@RequirePermission("story:manage")`）

**订单扩展**
- `POST /api/orders/{id}/upgrade-room`：主人升级房型（PENDING_PAYMENT/PAID/CONFIRMED），自动算差价并生成 SUCCESS 补差流水；目标房型限制同分区且价格更高

**新增安全能力**
- `POST /api/auth/captcha/verify`：验证码即时校验（不销毁，供分步表单"下一步"预检）
- 登录/注册均校验图形验证码；`PUT /api/auth/password` 与重置密码在校验旧密码/手机号+图形码之外，已设置密保的用户需同时回答密保答案
- `POST /api/auth/security-question`：设置/修改密保问题（答案 BCrypt 存储）
- 细粒度权限注解 `@RequirePermission`（切面校验 sys_role.permissions，支持 `*:` 通配），用于房型管理（room:type:manage）、房间状态（room:status）、评价回复（review:reply）、营销/故事管理端接口
