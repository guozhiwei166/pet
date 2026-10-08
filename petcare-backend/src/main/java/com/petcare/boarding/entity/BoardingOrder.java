package com.petcare.boarding.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("boarding_order")
public class BoardingOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long petId;
    private Long roomTypeId;
    private Long roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer days;
    private BigDecimal roomAmount;
    private BigDecimal serviceAmount;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private String status;
    private String contactName;
    private String contactPhone;
    private String remark;
    private LocalDateTime payTime;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private String cancelReason;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
