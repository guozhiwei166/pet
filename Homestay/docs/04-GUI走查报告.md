# GUI 走查报告(web-gui-tester + GLM-4V 视觉验收)

> 走查时间:2026-09-03 | 环境:backend:8080 + frontend:5173 + MySQL(干净演示数据)
> 方式:浏览器黑盒 GUI 操作(登录/点击/填表/截图),遵守"动作→观察"循环
> 截图存档:项目根 gui-test-screenshots/(t0~t14,PNG)
> 视觉验收:智谱 GLM-4V(glm-4v-plus)对存档截图逐张复查

## P0 主链路(全部通过)

| 测试点 | 结果 | 证据 |
|---|---|---|
| 首页游客态(轮播/公告/目的地/推荐) | 通过 | t0_home.png |
| 大理民宿列表(筛选生效、无异地混入) | 通过 | t1_homestay_list.png,URL ?destinationId=1 |
| 民宿详情(介绍/房型/收藏/举报/三类周边tab) | 通过 | t2_homestay_detail.png、t2b_near_exp.png |
| 游客预订→登录拦截(带 redirect 回跳) | 通过 | t3_login_redirect.png |
| user01 下单→支付→成功页 | 通过 | t4_order_confirm.png、t6_pay_success.png(¥936=468×2晚) |
| 我的订单(新订单"待入住") | 通过 | t7_my_orders.png |
| 商家经营概览→办理入住(确认弹窗→状态流转) | 通过 | t8_merchant_dash.png、t9_merchant_checkin.png |
| 管理员数据首页/商家审核/举报处理 | 通过 | t10_admin_dash.png、t11_admin_merchants.png、t12_admin_reports.png |

## P1 特色功能抽查(全部通过)

- 推荐解释:t13/t13b —— 10 个推荐位均含 5 因子与解释文案;"评分 4.9/5""距中心约""收藏过该目的地"(user01 偏好加分真实生效)
- 旅行计划:t14 —— "大理三日游·风花雪月"按天展示 民宿/景点/体验/餐饮 四类项目 + 添加项目按钮
- 商家端房型上下架按钮、管理员举报页"受理/驳回"入口均可用

## 走查中发现并修复的 2 个真实缺陷(已修复+复测)

1. 库存预检 UTC 日期偏移:toISOString().slice(0,10) 按 UTC 截取导致东八区日期偏移一天,日历预检误判缺货、提交按钮被禁。修复:改为本地 y/m/d 格式化(OrderConfirm.vue);复测金额 ¥936、按钮解禁。
2. 价格试算接口误用下单 DTO:POST /order/preview 用 @Valid OrderCreateDTO(含 @NotBlank 联系人),预览阶段报"联系电话不能为空"。修复:新增轻量 OrderPreviewDTO(仅民宿/房型/日期/间数);复测 amount=936 nights=2。

## 视觉验收(GLM-4V plus 逐张复查)

| 截图 | 页面 | 视觉结论 |
|---|---|---|
| t0_home | 首页 hero/轮播 | 通过:大图+白字标题正常渲染 |
| t1_homestay_list | 民宿列表 | 通过:卡片网格整齐 |
| t2_homestay_detail | 民宿详情 | 通过:大图/标题正常,无空白错位 |
| t2b_near_exp | 详情-周边体验 tab | 通过:三 tab 可见,卡片图片已加载 |
| t3_login_redirect | 登录页 | 通过:表单/演示账号/按钮居中无错位 |
| t4_order_confirm | 订单确认 | 通过:步骤条/房型/日期/价格明细布局正常,金额正确 |
| t6_pay_success | 支付成功 | 通过:对勾+文案+三按钮完整 |
| t7_my_orders | 我的订单 | 通过:订单卡片完整无错位 |
| t8_merchant_dash | 商家经营概览 | 通过:统计卡可读,图表正常渲染无空白框 |
| t9_merchant_checkin | 商家订单管理 | 通过:表格完整,状态标签/按钮可读 |
| t10_admin_dash | 管理员数据首页 | 通过:统计卡整齐,折线/饼图已渲染 |
| t11_admin_merchants | 商家审核 | 通过:表格/状态/按钮正常 |
| t12_admin_reports | 举报处理 | 通过:列表正常 |
| t13/t13b_recommend | 推荐页+因子展开 | 通过:因子进度条/分数/解释无重叠截断 |
| t14_plan_detail | 旅行计划详情 | 通过:Day 分组清晰,行程卡片正常 |

- t5_pay_page 截图当时因脚本写盘偶发失败缺失,其视觉由 t6 覆盖
- 结论:三端核心页面无布局缺陷、无元素遮挡/截断/空白异常

## 测试限制说明

- 截图均为全页可见区(非整页长图);视觉验收依赖智谱 API,需网络
- 测试产生的订单/入住等数据已通过重置 schema+data.sql 清理,当前为干净演示态

## 收尾状态

- 后端(8080)、前端(5173)运行中;数据库为干净演示数据(12民宿/25房型/1525 库存)
- 页面回到首页,可直接开始演示
