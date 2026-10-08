# PROJECT_PLAN — 猫狗宠物寄养系统 开发路线

> 原则：先设计后开发；先基础架构后业务；先业务闭环后视觉精修；每个 Phase 结束必须"编译 → 启动 → 冒烟 → 修复"后再进入下一阶段。
> 新代码位置：`petcare-backend/`（com.petcare）+ `petcare-frontend/`（Vue3 + TS）。旧 `backend/`、`h5/`、`console/` 原地保留，切换完成后再统一归档。

## Phase 0 — 环境与规划（本次已完成）

- [x] PROJECT_ANALYSIS / DATABASE_DESIGN / API_DESIGN / UI_DESIGN / PROJECT_PLAN
- [x] Redis 环境确认：`C:\Users\L.L\Tools\Redis\redis-server.exe`（开发脚本负责启动，`redis-server.exe` 即开即用）
- [x] 创建 MySQL 库 `petcare`（全新，不动 pawstay 库）
- [x] Redis 已启动并接入（6379）
- 验收：文档评审通过，目录骨架建好 ✅

## Phase 1 — 后端基础架构

- com.petcare 骨架：common(result/exception/enums/utils) + config
- Spring Security + JWT（登录/刷新/退出、BCrypt、按角色路径守卫 /user/** /staff/** /admin/**）
- sys_user / sys_role / sys_user_role（+预留 permission 表）、注册（三步合并提交 + Redis 验证码）、登录失败锁定
- 统一 Result/GlobalExceptionHandler/参数校验、MyBatis-Plus（分页/逻辑删除/自动填充 create_time,update_time）
- 本地文件上传 FileStorageService、operation_log 切面（@OperationLog 注解）
- Redis 接入：连接配置、验证码、未读数、Token 黑名单；所有读缓存带降级回源
- 员工档案 employee 实体（staff 演示账号关联）
- 验收：注册→登录→me→改密码→上传头像→日志落库，冒烟脚本 v1 ✅（petcare-smoke.mjs 11 项通过，端口 8082）

## Phase 2 — 宠物体系

- pet / pet_tag / pet_tag_relation / pet_vaccine / pet_health 实体与接口
- 宠物卡片列表、档案详情页（基础资料/标签/注意事项/疫苗状态计算）
- 成长档案聚合接口（寄养次数、体重曲线）
- 前端：用户端框架（顶部导航 + 路由守卫）、登录页、注册页（Stepper）、我的宠物、宠物档案页
- 验收：建宠物→传照→打标签→录疫苗→档案页四 Tab 正常

## Phase 3 — 房间与服务体系

- room_type / room / service 实体与接口；房间地图聚合接口
- 前端：管理端框架（深色侧栏布局）、房间地图（格子 + Drawer）、房型管理、服务管理
- 验收：增删改房型/房间/服务，地图状态颜色正确

## Phase 4 — 预约订单（核心）

- availability 计算（日历库存色）、创建订单（事务 + 算价 + 库存并发控制）
- 订单 8 态状态机 + 支付流水（模拟支付）+ 退款流
- 前端：我要寄养五步 Stepper（实时算价）、我的订单卡片流、订单详情（OrderProgress + 支付/取消/退款）
- 管理端：订单列表 + 确认订单
- 验收：完整下单→支付→确认链路；并发下单最后一间房仅一人成功

## Phase 5 — 照护体系（员工端）

- 入住/开始寄养/离店接口（房间状态联动、宠物体重回写）
- care_task 生成器（入住确认后按天批量生成）+ 五类记录表
- 前端：员工今日工作台（问候 + 统计 + 任务卡流 + 完成小表单）、宠物注意事项置顶、房间管理手工状态
- 验收：员工登录即见今日任务，完成任务落记录

## Phase 6 — 日报 / 异常 / 通知（核心创新）

- pet_daily_report 汇总生成器（任务完成时触发更新）
- pet_exception 全生命周期 + 通知规则（NORMAL→管理员，SEVERE→加主人）
- notification 统一通知中心（分类/未读/跳转业务）
- 前端：日报卡组件、寄养时间线、异常上报表单、异常中心看板、铃铛通知中心
- 验收：员工打卡→主人生成日报；上报异常→流转→通知→关闭 全链路

## Phase 7 — 评价与成长档案

- COMPLETED 后评价（五维 + 标签）、管理员回复；评价进订单/宠物统计
- 前端：评价弹窗（五维星级 + 标签）、宠物档案"成长档案"Tab（体重曲线 + 累计统计）
- 验收：离店→评价→回复→档案数据更新

## Phase 8 — 管理驾驶舱与系统管理

- dashboard 聚合（Redis 缓存 60s）、趋势/热门服务/员工完成率统计
- 用户管理（禁用）、宠物管理（全店视角）、操作日志查询
- 前端：驾驶舱（四指标 + 使用率环 + 状态分布 + 趋势折线 + 热门服务条 + 待处理异常）
- 验收：大屏数据与数据库可对账

## Phase 9 — UI 精修与最终验收

- 全站 UI 走查（按 UI_DESIGN 第 95 节清单逐页核对：颜色/字体/圆角/间距/空态/Loading/错误态/Hover/Focus）
- 演示数据美化（真实业务语料：豆豆/金毛/203豪华房/员工李晓宇…）
- e2e 冒烟脚本（按规格第 93 节完整演示链路）
- 旧版本归档：`backend/`、`h5/`、`console/` 移入 `legacy/`（经确认后执行）
- 验收：第 93 节演示流程全程可跑，第 96 节验收清单全勾

## 复用映射速查

| 新模块 | 从旧代码复用 |
|---|---|
| Result/异常/校验/MP 配置 | backend/common、config 直接迁移改造 |
| Spring Security | 参考旧 AuthInterceptor 白名单思路，替换为过滤器链 |
| 订单状态机 | 旧 OrderService 状态校验方法扩为 8 态 |
| ECharts 大屏/体重曲线 | console/AdminDashboard、h5/OrderDetail 配置迁移 |
| 时间线/插画组件 | h5 时间线样式、Illust SVG 插画（换鼠尾草绿配色） |
| 双库兼容 SQL 写法 | data.sql 的 `NOW() - INTERVAL 'n' DAY` |
| 冒烟测试框架 | smoke-test.mjs 升级 |
