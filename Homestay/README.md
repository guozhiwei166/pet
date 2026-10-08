# 基于 Spring Boot 的旅游民宿综合服务平台

面向 **游客 / 民宿商家 / 平台管理员** 的毕业设计级旅游民宿综合服务平台。
核心特色:民宿预订闭环 + 民宿×周边旅游资源关联 + 个人旅行计划 + 可解释加权推荐 + 商家/平台经营统计。

## 技术栈

| 端 | 技术 |
|---|---|
| 后端 | Java 17 / Spring Boot 3.2.x / MyBatis-Plus / MySQL 8 / JWT / BCrypt / Maven |
| 前端 | Vue 3 / Vite / Vue Router / Pinia / Element Plus / Axios / ECharts |

> 说明:JWT 权限采用"拦截器 + @RequireRole 注解"实现,未引入 Spring Security(权限点更清晰,便于答辩讲解);
> BCrypt 密码加密、统一 Result、全局异常、Jakarta Validation 参数校验均保留。

## 目录结构

```
Homestay/
├─ backend/                  Spring Boot 后端
│  ├─ src/main/java/com/tourism/
│  │  ├─ common       统一返回/异常/常量/分页
│  │  ├─ config       Web/MyBatis-Plus 配置
│  │  ├─ security     JwtUtil/JwtInterceptor/@RequireRole/LoginUser
│  │  ├─ controller   controller 层(按业务域)
│  │  ├─ service + impl
│  │  ├─ mapper / entity / dto / vo
│  │  └─ task         定时取消超时订单
│  └─ sql/ schema.sql(建库建表) + data.sql(初始化演示数据)
└─ frontend/                 Vue3 前端(用户端+商家端+管理员端)
```

## 快速启动

### 1. 准备数据库(MySQL 8)

```sql
-- 方式一:命令行
mysql -uroot -p < backend/sql/schema.sql
mysql -uroot -p tourism_homestay < backend/sql/data.sql

-- 方式二:Navicat 分别运行 backend/sql/schema.sql、data.sql 两个脚本
```

### 2. 启动后端

1. 修改 `backend/src/main/resources/application.yml` 中数据库账号密码(默认 root/root);
2. IDEA 打开 `backend`,运行 `com.tourism.TourismApplication`(或 `mvn spring-boot:run`);
3. 后端地址 `http://localhost:8080`。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173`(已配置 /api 代理到 8080)。

## 演示账号(密码统一 123456)

> 说明:为面向正式访客,登录页已不展示演示账号;答辩演示时按此表手动输入即可。

| 角色 | 账号 | 说明 |
|---|---|---|
| 管理员 | `admin` | 平台数据/审核/举报/统计 |
| 商家(大理) | `merchant_dali` | 洱海之恋等 6 家大理民宿,已通过审核 |
| 商家(丽江) | `merchant_lijiang` | 古城/束河民宿 |
| 商家(待审核) | `merchant_pending` | 演示"商家审核"流程 |
| 普通用户 | `user01` ~ `user07` | user01 有历史订单/收藏/旅行计划演示数据 |

## 一图看懂业务闭环

```
游客注册/登录 → 浏览目的地/搜索民宿 → 民宿详情(房型+周边景点/体验/餐饮)
→ 选日期下单(库存行锁防超卖) → 模拟支付(微信/支付宝/银行卡) → 我的订单
→ 商家办理入住/退房 → 完成订单评价(五维评分) → 商家回复
   │
   └─ 特色:民宿×周边资源关联 / 个人旅行计划(民宿+景点+体验+餐饮 按天编排)
          / 可解释民宿推荐(评分40%+价格20%+距离20%+热门10%+偏好10%)
```

## 演示建议流程

1. **游客浏览**:首页 → 选"大理" → 民宿列表 → 民宿详情看"周边景点/体验/餐饮"(创新点①);
2. **预订闭环**:选房型日期 → 确认 → 模拟支付 → 我的订单;演示库存不足/取消恢复;
3. **旅行计划**(创新点②):创建"大理三日游",把民宿/景点/体验/餐厅加入计划并按天查看;
4. **推荐**(创新点③):"为你推荐"页查看推荐分与可解释因子;
5. **评价**:完成后评价 + 切换商家回复;
6. **商家视角**:`merchant_dali` 看订单/入住/收入/入住率图表;
7. **管理员视角**:`admin` 看平台统计、审核待审核商家/民宿、处理举报。

## 设计要点(可写进论文)

- 订单状态机:PENDING→PAID→CHECKED_IN→COMPLETED,可 CANCELLED;支付幂等、超时自动取消。
- 防超卖:房型×日期库存表(room_inventory)唯一键 + 事务内 `SELECT…FOR UPDATE` 行锁 + 原子扣减。
- 民宿与资源多对多关联(homestay_scenic / homestay_experience / homestay_restaurant)。
- 推荐模型:多因子加权,附因子解释;无机器学习,可解释、可运行。
- 删除保护:已产生订单的民宿/房型不可删;商家只能操作自己的民宿;用户只能评价自己的已完成订单。

## 常见问题

- **图片加载**:演示数据图片使用 `picsum.photos` 外链,需联网;离线时可改用本地上传(后端 /api/upload/image,图片存 uploads/ 目录)。
- **端口冲突**:后端 8080、前端 5173,可在 application.yml / vite.config.js 修改。
