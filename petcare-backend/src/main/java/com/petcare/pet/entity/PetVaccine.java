package com.petcare.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pet_vaccine")
public class PetVaccine {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long petId;
    private String vaccineName;
    private LocalDate inoculationDate;
    private LocalDate expireDate;
    private String org;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
