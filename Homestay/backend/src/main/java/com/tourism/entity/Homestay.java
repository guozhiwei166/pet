package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 民宿
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("homestay")
public class Homestay extends BaseEntity {

    private Long merchantId;
    private Long destinationId;
    private String name;
    private String coverImage;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String intro;
    private String description;
    /** 特色,逗号分隔 */
    private String features;
    /** 设施,逗号分隔 */
    private String facilities;
    private String phone;
    /** OPEN / CLOSED */
    private String businessStatus;
    /** PENDING / APPROVED / REJECTED / OFFLINE */
    private String auditStatus;
    private BigDecimal rating;
    private Integer ratingCount;
    private Integer favoriteCount;
    private Integer viewCount;
    private BigDecimal minPrice;
    private Integer sortOrder;
    private LocalDateTime auditTime;
    private String auditRemark;
}
