package com.petcare.marketing.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 用户已领取优惠券 */
@Data
@TableName("user_coupon")
public class UserCoupon {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long couponId;
    private Long userId;
    private String status;
    private Long usedOrderId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime getTime;
    private LocalDateTime useTime;
}
