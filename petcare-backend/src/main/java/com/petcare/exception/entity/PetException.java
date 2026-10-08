package com.petcare.exception.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pet_exception")
public class PetException {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long petId;
    private Long orderId;
    private String type;
    private String level;
    private String description;
    private String photos;
    private String status;
    private Long reportStaffId;
    private Long handlerId;
    private String handleNote;
    private LocalDateTime handleTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
