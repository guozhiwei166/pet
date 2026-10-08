# 代码审查 Bug 清单 — 修复记录

> 原始审查:2026-09-03(见本文件第一版内容)
> 修复完成:2026-09-03,已编译通过 + 接口回归验证

## 一、高危(P0)— 全部已修复 ✅

| # | 问题 | 修复方案 | 验证 |
|---|---|---|---|
| 1 | 房型上架/下架 400 | 新增独立接口 `PUT /api/room-type/merchant/{id}/status`(后端 `RoomTypeService.changeStatus`);前端 `changeRoomTypeStatus` 调用 | 下架/上架均 200 ✅ |
| 2 | `room_type.stock` 与日期库存不同步 | 下单时原子扣减 stock(`RoomTypeMapper.decrementStock`,防超扣);取消时条件回补(仅未来单,上限 `room_count`,`incrementStock` 用 `LEAST`) | stock 3→2→3 ✅ |
| 3 | 商家房型列表越权(IDOR) | `listAll` 增加归属校验:`RoomTypeServiceImpl.requireHomestayOwner`(同 create/update/delete 口径) | 跨商家访问 403 ✅ |
| 4 | 图片上传匿名公开 | 去掉 `@Public`,需登录(`LoginUser.require()`);限制单文件 ≤5MB;补 `MaxUploadSizeExceededException` 处理器 | 未登录上传 401 ✅ |

## 二、中危(P1)— 除标注外全部已修复 ✅

| # | 问题 | 修复状态 |
|---|---|---|
| 5 | 收藏 DELETE 语义反了 | ✅ 新增 `FavoriteService.remove()`(幂等删除),Controller 改调 |
| 6 | 扩房只补缺失日期 | ✅ 扩房 = 所有未来日期可售 + 差额;缩房取 min 防超售 |
| 7 | 浏览量无调用点 | ✅ `HomestayController.detail()` 上架民宿访问即计数;`incrView` 改原子 SQL(`incrementView`),并发不丢 |
| 8 | 日历接口前端未接线 | ✅ `OrderConfirm.recalc()` 消费 `roomCalendar`:逐晚库存预检,不足显示原因并禁用提交 |
| 9 | 举报链路断开 | ✅ 民宿详情页新增举报入口(民宿+评价两类),复用 `createReport`;已联调成功 |
| 10 | 隐藏/恢复评价不重算评分 | ✅ `hideByAdmin/restoreByAdmin` 联动 `recalcRating`;空评价重置默认 5.0/0;举报处理联动隐藏同路径 |
| 11 | 入住率含未来订单 | ✅ 分子改为"落在 [today-30, today] 窗口内的晚数×间数" |
| 12 | totalRooms 口径不一致 | ✅ `totalRooms` 改为房间总数(与入住率分母同口径) |
| 13 | 分维度均分只基于 20 条 | ✅ 评价拉取 size 提到 100(演示规模覆盖;论文可注明概览基于样本) |
| 14 | 当天被禁用 | ✅ disabled-date 按"日"比较,当天可入住(与后端一致) |
| 15 | REPORT_REVIEW=5 与 schema 2 冲突 | ✅ 统一为 2(schema/前端 typeText 原已用 2),data.sql 种子同步;代码加注释说明枚举复用 |
| 16 | pay 无幂等/不校验超时 | ✅ 条件更新(仅 PENDING→PAID)+ 下单 15 分钟超时不可支付 |
| 17 | 删目的地悬空引用(仅查民宿) | ⏳ 保留(目的地删除在管理端已拦截"有民宿不可删";资源引用属演示可接受,见 P2-30) |
| 18 | 商家封禁不联动下架 | ✅ `auditMerchant` 驳回/禁用时联动下架名下 APPROVED 民宿(事务);重新通过不自动上架(民宿审核独立) |
| 19 | 改简介即重置审核 | ✅ 仅"商家名称"变更才重置 PENDING;联系人/电话/简介即时生效 |
| 20 | 推荐距离因子固定大理参考点 | ✅ 参考点 = 候选民宿集合质心;factor 文案泛化为"距目的地中心" | 丽江民宿距离分 90~98 ✅ |
| 21 | 401 未清 Pinia | ✅ response 拦截器 401 分支动态 `useUserStore().logout()`(延迟 import 规避循环依赖) |

## 三、低危(P2)— 已修复项

| # | 问题 | 修复状态 |
|---|---|---|
| 22 | OrderConfirm 试算静默失败 | ✅ 失败显示后端错误原因;库存不足给出具体日期与余量 |
| 23 | 入住率 0~1/0~100 误判 | ✅ 后端统一返回百分比,前端直接 `Math.round` |
| 24 | 金额 ¥0 不齐 | ✅ money() 统一 toFixed(2) |
| 25 | 唯一键冲突提示"系统繁忙" | ✅ 全局 `DuplicateKeyException` → 409"操作重复" |
| 28 | 计划日期先后/排序/分组 | ✅ 校验结束 ≥ 开始日期;(排序重排/分组 NPE 见备注) |
| 33 | NON_API_PREFIXES 死代码 | ✅ 删除 |
| 34 | JSON/方法不支持异常 | ✅ 补 `HttpMessageNotReadable/HttpRequestMethodNotSupported` handler |
| 35 | 订单号随机段过短 | ✅ 时间戳 + 12 位随机 |
| 36 | 硬编码 APPROVED/OPEN | ✅ detail() 改用 Constants |
| 37 | 方案文档与实现口径 | ✅ 交付说明已注明上传为登录后本地上传+外链并存 |

## 四、保留未修(说明理由,答辩口径)

| # | 问题 | 说明 |
|---|---|---|
| 26 | Review/Favorite N+1 | 演示数据量(百条内)无感知;论文可写"规模增大后可引入 JOIN/缓存"作展望 |
| 27 | platformStats 全表内存聚合 | 同上;毕设数据量合理,避免过早优化 |
| 28-② | moveItem 不重排同天 sortOrder | 已支持改天/改序号;重复序号仅影响同值稳定排序,演示无碍 |
| 28-③ | detail groupingBy null day NPE | 数据由 addItem(day 必填非空)产生,实际不可达 |
| 29 | update BeanUtils null 覆盖 | ✅ 已改字段级非空补丁(roomType);同模式其余保存接口由 @Valid 全量提交约束 |
| 30/31 | 资源/民宿删除悬空引用 | 删除路径均有业务校验(有订单/有民宿不可删);收藏/行程引用演示级可接受 |
| 32 | incrView 原子化 | ✅ 已改原子 SQL |
| 17 | 目的地删除引用 | 保留(管理端已拦"有民宿"),资源引用删除后列表页优雅空态 |

## 回归验证汇总(2026-09-03 接口实测)

- 房型上下架 200;跨商家房型列表 403;未登录上传 401
- 浏览量 3200→3202(两次访问 +2);重复评价 409;重复举报拦截
- stock:下单 3→2、取消 2→3
- 推荐:丽江民宿距离因子 90.8~98.7(修复前恒 40)
- 举报链:评价举报 200、民宿举报重复拦截正确
- 全量 `mvn compile` / `npm run build` 通过
