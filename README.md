# 🐾 猫狗宠物寄养系统

本科毕业设计作品。以"宠物全生命周期照护"为核心的单店 PC Web 系统：**宠物数字档案 → 寄养预约 → 订单 → 员工任务 → 日常照护 → 宠物日报 → 异常事件闭环 → 通知 → 离店 → 评价 → 成长档案**。

三角色三套界面：宠物主人（温暖卡片式）、工作人员（任务驱动工作台）、管理员（运营驾驶舱）。

## 一、技术栈

| 层次 | 技术 |
|---|---|
| 后端 | Java 17 · Spring Boot 3.3.4 · **Spring Security** + JWT · MyBatis-Plus 3.5.7 · **Redis** · MySQL 8 · AOP 操作日志 |
| 前端 | Vue 3 · **TypeScript** · Vite 5 · Element Plus（鼠尾草绿自定义主题）· Pinia · ECharts 5 |

## 二、目录结构

```
Pet/
├── petcare-backend/     新版后端（com.petcare，模块化：auth/user/pet/room/boarding/care/exception/notification/review/payment/statistics）
├── petcare-frontend/    新版前端（三角色单应用：/ 用户端、/staff 员工端、/admin 管理端）
├── db/                  MySQL 脚本（petcare 库）
├── petcare-smoke.mjs          冒烟：基础+宠物（18 项）
├── petcare-smoke-booking.mjs  冒烟：订单+并发占房（12 项）
├── petcare-smoke-care.mjs     冒烟：照护任务（9 项）
├── petcare-smoke-report.mjs   冒烟：日报+异常+通知（7 项）
├── petcare-smoke-review.mjs   冒烟：评价（4 项）
├── petcare-smoke-e2e.mjs      完整演示链路 e2e（规格 93 节）
├── PROJECT_*.md / DATABASE_DESIGN.md / API_DESIGN.md / UI_DESIGN.md   设计文档
└── backend/ h5/ console/   旧版（PawStay 多商家方案，已停止演进，保留备查）
```

## 三、快速启动

```bash
# 0) 启动 Redis（若未运行）
C:\Users\L.L\Tools\Redis\redis-server.exe

# 1) 后端（自动建表+基础数据；录入真实数据后把 application.yml 的 sql.init.mode 改为 never）
cd petcare-backend && mvn spring-boot:run        # http://localhost:8082

# 2) 前端
cd petcare-frontend && npm install && npm run dev  # http://localhost:5190
```

演示账号（首启自动创建）：`user/user123`（宠物主人）、`staff/staff123`（员工 李晓宇）、`admin/admin123`（管理员）。

> 端口说明：8080/5173/5180/5181 被旧版本或其它程序占用，本项目使用 **8082 / 5190**。

## 四、功能总览

**用户端**：宠物数字档案（性格标签/疫苗/注意事项）· 五步预约 Stepper（宠物→日期→房型→增值服务→确认）· 模拟支付 · 订单进度 · 每日照护日报 · 当前寄养进度 · 体重健康曲线 · 五维评价 · 通知中心 · 个人资料/密码

**员工端**：今日工作台（任务统计+卡片流）· 照护打卡（喂食/遛宠/健康观察/喂药，完成即落记录并刷新日报）· 宠物注意事项置顶 · 异常上报 · 房间状态管理

**管理端**：运营驾驶舱（今日营业额/入住/使用率/趋势/热门服务/员工完成率）· 订单管理（确认并原子占房/退款审批）· 房间地图 · 房型/服务管理 · 用户禁用 · 操作日志

## 五、核心设计（答辩讲解索引）

| 主题 | 位置 |
|---|---|
| 订单 9 态状态机（后端唯一裁决） | `boarding/service/BoardingService` |
| 原子占房（并发条件更新） | `BoardingService.confirm` |
| 下单防超卖（房型行锁串行化） | `BoardingService.create` + `RoomTypeMapper.selectByIdForUpdate` |
| 照护任务生成器 | `care/service/CareService.generateTasks` |
| 日报自动汇总 | `care/service/DailyReportService.refresh` |
| 异常事件生命周期 + 分级通知 | `exception/service/PetExceptionService` |
| 权限（JWT + 角色 + 细粒度权限码切面） | `config/SecurityConfig`、`security/`（`@RequirePermission` + `PermissionAspect`） |
| 操作日志切面 | `log/LogAspect` + `@OperationLog` |
| 鼠尾草绿设计令牌 | `petcare-frontend/src/styles/theme.css` |

## 四·五、扩展模块（后续迭代新增）

项目在核心寄养链路之外，还实现了三个面向运营/社区的模块（答辩可作"扩展性"讲解）：

| 模块 | 包/前端 | 说明 |
|---|---|---|
| 门店服务预约 store | `backend .../store`、`frontend .../store/` | 门店信息、洗护美容等增值服务、服务预约（确认/完成流转） |
| 营销 marketing | `.../marketing` | 优惠券模板与领取（满减/折扣）、平台公告；折扣券按 `percent`（80=8折）计算优惠 |
| 故事墙 story | `.../story` | 主人发布图文故事、点赞、管理员审核（下架/恢复） |

房型分区：犬区 4 档（标准/舒适/豪华/VIP）+ 猫区 4 档（`猫·` 前缀，同价对应），下单页按宠物物种只显示对应分区；房间 3 层 × 每层 20 间。

数据持久化：`application.yml` 默认 `sql.init.mode: never`（重启不重置）；恢复演示数据用 `-Dspring-boot.run.profiles=demo` 启动一次。

## 六、测试

```bash
node petcare-smoke.mjs          # 基础+宠物
node petcare-smoke-booking.mjs  # 订单全链路 + 并发占房
node petcare-smoke-care.mjs     # 照护任务
node petcare-smoke-report.mjs   # 日报+异常+通知
node petcare-smoke-review.mjs   # 评价
node petcare-smoke-e2e.mjs      # 完整演示链路（规格 93 节）
```

## 七、已知说明与展望

- 图片本地存储（`FileStorageService`），预留 OSS 扩展；支付为模拟（保留 payment 流水模型，可接支付宝沙箱）
- 通知为 20 秒轮询 + Redis 未读数缓存，可升级 WebSocket
- 权限采用"角色码 + 路径守卫 + 方法注解"三重控制，sys_permission 表为预留扩展
- 展望：微信小程序端、宠物健康趋势预警、多门店支持
