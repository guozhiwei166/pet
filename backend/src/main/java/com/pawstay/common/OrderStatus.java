package com.pawstay.common;

/**
 * 寄养订单状态机：待确认 → 已接单 → 寄养中 → 已完成；可取消/拒单
 */
public interface OrderStatus {
    String PENDING_CONFIRM = "PENDING_CONFIRM"; // 待商家接单
    String CONFIRMED = "CONFIRMED";             // 已接单·待入住
    String BOARDING = "BOARDING";               // 寄养中
    String COMPLETED = "COMPLETED";             // 已完成
    String CANCELLED = "CANCELLED";             // 已取消
    String REJECTED = "REJECTED";               // 商家拒单
}
