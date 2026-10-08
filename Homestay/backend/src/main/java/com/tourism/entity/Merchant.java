package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 民宿商家
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchant")
public class Merchant extends BaseEntity {

    private Long userId;
    private String merchantName;
    private String contactName;
    private String contactPhone;
    private String licenseNo;
    private String description;
    /** PENDING / APPROVED / REJECTED / DISABLED */
    private String status;
    private LocalDateTime auditTime;
    private String auditRemark;
}
