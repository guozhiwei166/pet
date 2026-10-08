package com.tourism.common;

/**
 * 全局业务常量:角色 / 状态 / 类型,均用语义化字符串
 */
public interface Constants {

    // ============ 角色 ============
    String ROLE_USER = "USER";
    String ROLE_MERCHANT = "MERCHANT";
    String ROLE_ADMIN = "ADMIN";

    // ============ 通用状态 ============
    /** 用户状态 */
    int USER_STATUS_NORMAL = 0;
    int USER_STATUS_DISABLED = 1;

    /** 审核状态:民宿/资源/商家 */
    String AUDIT_PENDING = "PENDING";    // 待审核
    String AUDIT_APPROVED = "APPROVED";  // 已通过
    String AUDIT_REJECTED = "REJECTED";  // 已拒绝
    String AUDIT_OFFLINE = "OFFLINE";    // 已下架(民宿/资源特有,商家不适用)

    /** 民宿营业状态 */
    String BIZ_OPEN = "OPEN";
    String BIZ_CLOSED = "CLOSED";

    /** 房型上下架 */
    String ROOM_ON = "ON";
    String ROOM_OFF = "OFF";

    // ============ 订单状态 ============
    String ORDER_PENDING = "PENDING";        // 待支付
    String ORDER_PAID = "PAID";              // 已支付(待入住)
    String ORDER_ARRIVED = "ARRIVED";        // 已到店(客人已到达,待商家接待)
    String ORDER_CHECKED_IN = "CHECKED_IN";  // 入住中
    String ORDER_COMPLETED = "COMPLETED";    // 已完成
    String ORDER_CANCELLED = "CANCELLED";    // 已取消

    // ============ 支付 ============
    String PAY_UNPAID = "UNPAID";
    String PAY_PAID = "PAID";
    String PAY_REFUNDED = "REFUNDED";

    String PAY_TYPE_WECHAT = "WECHAT";
    String PAY_TYPE_ALIPAY = "ALIPAY";
    String PAY_TYPE_BANK = "BANK";

    // ============ 收藏 / 举报 / 资源 类型 ============
    /** 收藏目标类型 */
    int TARGET_HOMESTAY = 1;
    int TARGET_SCENIC = 2;
    int TARGET_EXPERIENCE = 3;
    int TARGET_RESTAURANT = 4;

    /** 举报目标类型(1=民宿 2=评价 3=体验 4=餐饮,与 schema 注释一致;注意与收藏类型枚举值相同但语义不同) */
    int REPORT_HOMESTAY = 1;
    int REPORT_REVIEW = 2;

    // ============ 旅行计划 ============
    String ITEM_HOMESTAY = "HOMESTAY";
    String ITEM_SCENIC = "SCENIC";
    String ITEM_EXPERIENCE = "EXPERIENCE";
    String ITEM_RESTAURANT = "RESTAURANT";

    String PLAN_ONGOING = "ONGOING";
    String PLAN_FINISHED = "FINISHED";
    String PLAN_CANCELLED = "CANCELLED";

    // ============ 评价 ============
    int REVIEW_VISIBLE = 0;
    int REVIEW_HIDDEN = 1;

    // ============ 举报状态 ============
    String REPORT_PENDING = "PENDING";
    String REPORT_HANDLING = "HANDLING";
    String REPORT_HANDLED = "HANDLED";
    String REPORT_REJECTED = "REJECTED";

    // ============ 数据字典类别 ============
    String DICT_REPORT_REASON = "report_reason";

    /** 待支付自动取消分钟数 */
    int ORDER_EXPIRE_MINUTES = 15;
}
