package com.petcare.marketing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 我的优惠券视图：UserCoupon + 关联券模板信息，供前端直接渲染 */
@Data
public class MyCouponVO {
    private Long id;
    private Long couponId;
    private Long userId;
    private String status;
    private Long usedOrderId;
    private LocalDateTime getTime;
    private LocalDateTime useTime;

    // 券模板冗余字段（来自 coupon 表）
    private String name;
    private String type;
    private BigDecimal threshold;
    private BigDecimal amount;
    private Integer percent;
    private LocalDate startDate;
    private LocalDate endDate;
}
