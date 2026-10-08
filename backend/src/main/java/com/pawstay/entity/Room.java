package com.pawstay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pet_room")
public class Room {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long merchantId;
    private String name;
    private String species;         // CAT / DOG / ALL
    private BigDecimal maxWeight;   // 收纳体重上限 kg
    private Integer needSterilized; // 1 要求已绝育（猫舍混住常见要求）
    private Integer allowAggressive;// 0 不接收有攻击性宠物
    private Integer totalCount;     // 同型笼位数量
    private BigDecimal pricePerDay;
    private String photo;
    private String tags;
    private String description;
    private Integer status;         // 1 上架
    private LocalDateTime createTime;
}
