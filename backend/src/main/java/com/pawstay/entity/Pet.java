package com.pawstay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pet_pet")
public class Pet {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String species;         // CAT / DOG
    private String breed;
    private String gender;          // M / F
    private LocalDate birthDate;
    private BigDecimal weight;      // kg
    private Integer sterilized;     // 1 已绝育
    private String temperament;     // 温顺 / 活泼 / 警惕
    private Integer aggressive;     // 1 有攻击性（智能排笼约束项）
    private Integer vaccinated;     // 1 疫苗齐全
    private String vaccineNote;
    private String photo;           // Base64（演示环境），生产建议对象存储
    private String notes;
    private LocalDateTime createTime;
}
