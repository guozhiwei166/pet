package com.tourism.util;

import com.tourism.common.Constants;

import java.util.Map;

/**
 * 状态码 -> 中文文案,供 VO/前端展示
 */
public class StatusTextUtil {

    private static final Map<String, String> ORDER = Map.of(
            Constants.ORDER_PENDING, "待支付",
            Constants.ORDER_PAID, "待入住",
            Constants.ORDER_ARRIVED, "已到店",
            Constants.ORDER_CHECKED_IN, "入住中",
            Constants.ORDER_COMPLETED, "已完成",
            Constants.ORDER_CANCELLED, "已取消"
    );

    private static final Map<String, String> AUDIT = Map.of(
            Constants.AUDIT_PENDING, "待审核",
            Constants.AUDIT_APPROVED, "已通过",
            Constants.AUDIT_REJECTED, "已拒绝",
            Constants.AUDIT_OFFLINE, "已下架"
    );

    private static final Map<String, String> PAY = Map.of(
            Constants.PAY_UNPAID, "未支付",
            Constants.PAY_PAID, "已支付",
            Constants.PAY_REFUNDED, "已退款"
    );

    private static final Map<String, String> MERCHANT = Map.of(
            Constants.AUDIT_PENDING, "待审核",
            Constants.AUDIT_APPROVED, "已通过",
            Constants.AUDIT_REJECTED, "已驳回",
            "DISABLED", "已禁用"
    );

    private static final Map<String, String> REPORT = Map.of(
            Constants.REPORT_PENDING, "待处理",
            Constants.REPORT_HANDLING, "处理中",
            Constants.REPORT_HANDLED, "已处理",
            Constants.REPORT_REJECTED, "已驳回"
    );

    public static String order(String s) { return ORDER.getOrDefault(s, s); }
    public static String audit(String s) { return AUDIT.getOrDefault(s, s); }
    public static String pay(String s) { return PAY.getOrDefault(s, s); }
    public static String merchant(String s) { return MERCHANT.getOrDefault(s, s); }
    public static String report(String s) { return REPORT.getOrDefault(s, s); }
}
