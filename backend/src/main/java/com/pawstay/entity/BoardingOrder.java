package com.pawstay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pet_order")
public class BoardingOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long petId;
    private Long merchantId;
    private Long roomId;
    private LocalDate startDate;    // 入住日
    private LocalDate endDate;      // 离店日
    private Integer days;
    private BigDecimal pricePerDay;
    private BigDecimal totalAmount;
    private String status;
    private String contactName;
    private String contactPhone;
    private String remark;
    private Integer agreementSigned; // 1 已同意电子寄养协议
    private LocalDateTime payTime;       // 模拟支付时间
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private BigDecimal weightIn;    // 入住称重
    private BigDecimal weightOut;   // 离店称重（对比形成健康曲线）
    private String rejectReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
