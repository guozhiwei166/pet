package com.petcare.care.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pet_daily_report")
public class PetDailyReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long petId;
    private LocalDate reportDate;
    private String mentalState;
    private String appetite;
    private String water;
    private String poop;
    private Integer isAbnormal;
    private Integer activityMinutes;
    private String mealsSummary;
    private String staffNote;
    private String photos;
    private Long staffId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
