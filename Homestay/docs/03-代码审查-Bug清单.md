# 代码审查 Bug 清单

> 审查时间:2026-09-03
> 范围:backend 全部 Java 源码(8241 行 / 130 文件)+ frontend/src 全部源码(6921 行 / 53 文件)+ schema.sql
> 说明:以下均为在代码中**实际核实**的问题,按严重程度排序。行号对应当前代码。

---

## 一、高危(P0,功能直接不可用或数据错误)

### 1. 商家端「房型上架/下架」必定失败
- 前端 `frontend/src/views/merchant/RoomTypeManage.vue:181`
  ```js
  await saveRoomType({ id: row.id, homestayId, status: target })
  ```
- 后端 `backend/.../dto/RoomTypeSaveDTO.java:24,33,43,47` 对 `name`/`maxGuests`/`price`/`roomCount` 标了 `@NotNull`,
  且 `RoomTypeController.java:80` 用了 `@Valid`
- **后果**:点「上架/下架」直接返回 400「请填写可入住人数 / 请填写单价」,功能 100% 不可用
- **修复**:下架改为独立接口(如 `PUT /room-type/merchant/{id}/status`),或后端 update 只允许局部更新(忽略 null 字段)

### 2. 房型冗余库存 `stock` 只增不减,前端房态显示错误
- `OrderServiceImpl.create()`(第 87~129 行)**下单时只扣 `room_inventory`,从不扣 `room_type.stock`**
- 但 `OrderServiceImpl.restoreStock():353-357` 取消订单时却执行 `rt.setStock(rt.getStock() + roomCount)`
- 而前端正是用 `stock` 判断房态:
  - `HomestayDetail.vue:52,66,69` → "余X间" / "已满房" / 禁用预订按钮
  - `OrderConfirm.vue:30,34` → 房型是否可选、"余X间"
- **后果**:① 房间订完了前端仍显示"有房";② 每取消一单 stock 就 +N,数字越滚越大,可超过房间总数;
  ③ schema 注释也写明 stock 是"冗余展示",与实现自相矛盾
- **修复**:下单同步扣减 stock(或彻底删除 stock 字段,前端改读 `room_inventory` 日历接口)

### 3. 商家房型列表越权(IDOR)
- `backend/.../controller/RoomTypeController.java:65-70`
  ```java
  @GetMapping("/merchant/list")
  public Result<List<RoomType>> listByHomestay(@RequestParam Long homestayId) {
      LoginUser.requireRole("MERCHANT");       // 只校验了角色
      merchantService.requireApproved();       // 没校验民宿是否属于当前商家
      return Result.success(roomTypeService.listAll(homestayId));
  }
  ```
- 同类的 `create` / `update` / `delete` 都在 Service 里做了归属校验,**只有这一个漏了**
- **后果**:任意已审核商家传他人 `homestayId`,可拉取对方全部房型(含未上架房型的名称、价格、房间数)
- **修复**:`listAll` 前校验 `homestay.merchantId == 当前商家`(参照 `HomestayServiceImpl.requireOwner()`)

### 4. 图片上传接口完全公开,且无大小限制兜底
- `backend/.../controller/UploadController.java:38` 标注 `@Public` → **未登录任何人可上传文件**,存在磁盘写满/违规内容风险
- `GlobalExceptionHandler` 未处理 `MaxUploadSizeExceededException`,超限文件落到 `Exception` 兜底,前端只看到"系统繁忙"
- **后果**:匿名写入磁盘;上传失败无有效提示
- **修复**:改为登录后可传(`@Public` 去掉);补 `MaxUploadSizeExceededException` 处理器;限制单文件体积

---

## 二、中危(P1,逻辑错误 / 功能缺失 / 数据不一致)

| # | 位置 | 问题 | 后果 |
|---|---|---|---|
| 5 | `FavoriteController.java:48-52` | DELETE「取消收藏」接口内部调用的是 `favoriteService.toggle()` | 未收藏状态下调用会**变成新增收藏**,与 DELETE 语义相反 |
| 6 | `RoomTypeServiceImpl.java:104-127` | 扩房时只补**缺失日期**的库存,已存在日期(哪怕已被预订)不增加;与注释"补齐缺失日期的库存为新房间数"及缩房逻辑不对称 | 商家把房间数 3→10,大部分日期可售仍是 3 |
| 7 | `HomestayServiceImpl.java:118` `incrView()` | 定义了但**全项目无任何调用点**(已 grep 确认) | 浏览量恒为 0;`RecommendServiceImpl` 的"热门程度"因子、`orderByDesc(viewCount)` 排序全部失效 |
| 8 | `frontend/src/api/index.js:21` `roomCalendar` | 定义了但**前端无任何页面调用** | 后端 `/room-type/calendar` 库存日历接口白做,价格日历功能未落地 |
| 9 | `frontend/src/api/index.js:70` `createReport` | 定义了但**前端无调用**;管理端 `ReportManage.vue` 却已做好 | 用户无法提交举报,整个举报链路断开 |
| 10 | `ReviewServiceImpl.java:169-184` | `refreshHomestayRating()` 只在**新增评价**时重算;`hideByAdmin()`/`restoreByAdmin()`/`ReportServiceImpl.handle()` 隐藏评价后均不重算 | 管理员隐藏差评后,民宿评分仍包含该差评,监管失效 |
| 11 | `StatisticsServiceImpl.java:101-109` | 入住率分子 `occupiedNights` 用 `!checkInDate.isBefore(today-30)`,**包含未来入住日期的订单** | 入住率虚高,数据量大时可超过 100% |
| 12 | `StatisticsServiceImpl.java:94-95` | `totalRooms` 统计的是**房型种类数**,而同方法内入住率分母用的是**房间总数**(`mapToLong(RoomType::getRoomCount)`) | 同一 VO 内两个"房间数"口径不一致 |
| 13 | `frontend/src/views/homestay/HomestayDetail.vue:222-225` | `avgOf()` 只基于当前页 20 条评价算卫生/服务/设施/位置均分 | 评价超过 20 条后,分维度评分不准 |
| 14 | `frontend/src/views/order/OrderConfirm.vue:49` | `:disabled-date="d => d < new Date()"` 含时分秒 → **今天被禁用**;而后端 `OrderServiceImpl:328` 允许 `checkIn >= today` | 前后端不一致,当天不能下单 |
| 15 | `Constants.java:56` vs `schema.sql:410` | 代码 `REPORT_REVIEW = 5`,数据库注释写 `report_type 1=民宿 2=评价 3=体验 4=餐饮` | 类型编码前后端/文档不一致,接错直接报"举报类型不正确" |
| 16 | `OrderServiceImpl.pay():137-151` | 支付无幂等控制(未用乐观锁/条件更新),且**未校验订单是否已超时**(15 分钟未支付要等定时任务跑才失效) | 并发重复支付、超时订单仍可支付成功 |
| 17 | `AdminResourceServiceImpl.deleteDestination():87-93` | 只校验了民宿引用,未校验景点/体验/餐饮/收藏/旅行计划 | 删除目的地后产生大量悬空 `destination_id` |
| 18 | `AdminServiceImpl.auditMerchant():99-111` | 商家被驳回/禁用时,其名下已上架民宿**不会联动下架** | 被封禁商家的民宿仍可被正常预订 |
| 19 | `MerchantServiceImpl.updateProfile():63-67` | 商家只要修改资料(哪怕是简介),状态就被重置为 `PENDING` | 商家改一次简介就立刻失去经营权限,大概率非产品设计本意 |
| 20 | `RecommendServiceImpl.java:34-36,106-108` | 距离参考点硬编码为大理古城坐标(100.2676, 25.6065) | 非大理目的地的民宿距离分全部触底(恒 40),推荐算法中"距离匹配"因子(权重 0.2)形同虚设 |
| 21 | `frontend/src/api/request.js:27-34` | 401 时只清了 `localStorage`,**没有同步清空 Pinia store 的 `token`/`userInfo`** | 登录态残留在内存,`store.isLogin` 仍为 true,页面仍显示已登录,需手动刷新才恢复 |

---

## 三、低危(P2,健壮性 / 性能 / 体验)

| # | 位置 | 问题 |
|---|---|---|
| 22 | `OrderConfirm.vue:132-148` | `recalc()` 失败静默吞异常 → 页面显示"¥0.00"却不提示原因;且后端 `preview()` 本就不校验库存,要等提交订单才报"库存不足" |
| 23 | `MerchantDashboard.vue:117-121` | `occupancyText()` 兼容写法有边界错误:真实入住率为 0.5(%)时会被当成 0~1 比例,显示成 50% |
| 24 | `MerchantDashboard.vue:100-103` 等多处 | `money()` 对整数返回不带小数,`¥0` 而非 `¥0.00`,金额展示不齐 |
| 25 | `FavoriteServiceImpl.toggle()` / `ReviewServiceImpl.create()` | 并发去重依赖 DB 唯一索引兜底,`DuplicateKeyException` 落到 `Exception` 兜底 → 用户只看到"系统繁忙",而非"已收藏/已评价" |
| 26 | `ReviewServiceImpl.toVO():234-249` `FavoriteServiceImpl.toVO():173-213` | 每条记录 3~5 次单表查询,典型 N+1;评价列表/收藏列表数据量大后明显变慢 |
| 27 | `StatisticsServiceImpl.platformStats():182` | `ordersMapper.selectList(null)` 全表加载订单后在内存聚合,数据量大时内存与耗时都会爆 |
| 28 | `TravelPlanServiceImpl` | ①`create/update` 不校验起止日期先后;②`moveItem()` 不重排同天其它项的 `sortOrder`,会出现重复序号、排序不稳定;③`detail()` 中 `groupingBy(dayNumber)` 遇 null 会 NPE |
| 29 | `RoomTypeServiceImpl.java:88` | `int oldCount = rt.getRoomCount()` 对 `Integer` 拆箱,历史数据为 null 时 NPE;`update()` 中 Spring `BeanUtils.copyProperties` 会把 DTO 的 null 覆盖到实体 |
| 30 | `AdminResourceServiceImpl.deleteScenic/Experience/Restaurant()` | 删除资源前不校验是否被收藏表/行程表引用,会产生悬空引用 |
| 31 | `HomestayServiceImpl.delete():227-229` | 删除民宿时未清理 `room_inventory` 库存记录,留下孤儿数据 |
| 32 | `HomestayServiceImpl.incrView():118-124` | select-then-update 非原子,高并发下丢计数 |
| 33 | `JwtInterceptor.java:27` | `NON_API_PREFIXES` 常量定义后从未使用(死代码) |
| 34 | `GlobalExceptionHandler` | 未处理 `HttpMessageNotReadableException`(JSON 格式错)、`HttpRequestMethodNotSupportedException`,统一落到"系统繁忙" |
| 35 | `OrderServiceImpl.java:372-374` | 订单号 `HS + 时间戳 + 4 位随机`,极端并发下有重复风险(`order_no` 有唯一索引会直接报错) |
| 36 | `HomestayController.detail():61` | 硬编码字符串 `"APPROVED"`/`"OPEN"` 而非引用 `Constants`,易写错且重构时易漏 |
| 37 | `docs/01-项目开发方案.md` | 方案明确"图片统一 URL 存储、不落盘传文件",实际实现了落盘上传(`UploadController`),实现与方案不一致,答辩时需注意口径 |

---

## 四、审查结论

- **架构与规范整体是好的**:统一 Result、全局异常、JWT 拦截器 + 注解鉴权、防超卖用了 `FOR UPDATE` + 条件原子扣减、金额用 `BigDecimal`、DTO 参数校验齐全 —— 这些在毕设级项目里属于完成度较高的部分。
- **主要问题集中在三处**:
  1. **冗余字段 `room_type.stock` 与实际库存两套逻辑不同步**(P0-2),导致前端房态显示失真;
  2. **若干接口做了功能但没接线**(浏览量、库存日历、举报提交),或**接线了但参数对不上**(房型上下架);
  3. **越权与一致性校验有零星遗漏**(房型列表 IDOR、公开上传、评价隐藏不重算评分、商家封禁不联动下架)。
- **建议修复顺序**:先修 P0 的 1~4(可复现、影响演示),再处理 P1 中 5~11(答辩时容易被问到),P2 按时间取舍。
