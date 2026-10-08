package com.pawstay.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单列表视图：冗余宠物/商家/房型关键字段，避免前端多次请求拼装
 */
@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer days;
    private BigDecimal totalAmount;
    private BigDecimal pricePerDay;
    private Long petId;
    private String petName;
    private String petSpecies;
    private String petPhoto;
    private Long roomId;
    private String roomName;
    private Long merchantId;
    private String shopName;
    private String merchantPhone;
    private String contactName;
    private String contactPhone;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
}
