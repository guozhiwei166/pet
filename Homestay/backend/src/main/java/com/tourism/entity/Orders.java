package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预订订单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("orders")
public class Orders extends BaseEntity {

    private String orderNo;
    private Long userId;
    private Long homestayId;
    private Long roomTypeId;
    /** 以下为下单时快照,避免民宿/房型改动影响历史订单 */
    private String homestayName;
    private String homestayCover;
    private String roomTypeName;
    private java.time.LocalDate checkInDate;
    private java.time.LocalDate checkOutDate;
    private Integer nights;
    private Integer roomCount;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private String contactName;
    private String contactPhone;
    private String guestNames;
    /** PENDING / PAID / ARRIVED / CHECKED_IN / COMPLETED / CANCELLED */
    private String status;
    /** UNPAID / PAID / REFUNDED */
    private String payStatus;
    /** WECHAT / ALIPAY / BANK */
    private String payType;
    private String remark;
    private LocalDateTime cancelTime;
    private LocalDateTime payTime;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private LocalDateTime completeTime;
}
