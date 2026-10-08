package com.petcare.pet.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pet_health")
public class PetHealth {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long petId;
    private Long orderId;
    private LocalDate recordDate;
    private BigDecimal weight;
    private BigDecimal temperature;
    private String mentalState;
    private String appetite;
    private String water;
    private String poop;
    private Integer isAbnormal;
    private String remark;
    private Long staffId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
