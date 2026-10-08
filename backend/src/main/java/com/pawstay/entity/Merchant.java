package com.pawstay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pet_merchant")
public class Merchant {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String shopName;
    private String address;
    private String phone;
    private String license;         // 资质说明
    private String description;
    private String tags;
    private String serviceSpecies;  // 可寄养物种：CAT / DOG / CAT,DOG
    private BigDecimal rating;
    private Integer replyRate;      // 回复率 %
    private String auditStatus;     // PENDING 待审核 / APPROVED 已通过 / REJECTED 已驳回
    private String auditReason;
    private Integer boardingCount;  // 累计寄养次数
    private Integer openStatus;     // 1 营业中 0 休息中
    private LocalDateTime createTime;

    @TableField(exist = false)
    private BigDecimal minPrice;    // 首页联查：最低在售价
}
