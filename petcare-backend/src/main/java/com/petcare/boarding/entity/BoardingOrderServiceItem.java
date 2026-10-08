package com.petcare.boarding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("boarding_order_service")
public class BoardingOrderServiceItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long serviceId;
    private String serviceName;
    private BigDecimal price;
    private String unit;
    private Integer quantity;
    private BigDecimal amount;
}
