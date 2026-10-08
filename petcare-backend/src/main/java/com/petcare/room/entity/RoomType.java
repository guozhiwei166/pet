package com.petcare.room.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("room_type")
public class RoomType {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    /** 分区：DOG 犬区 / CAT 猫区（显式字段，下单时按宠物物种匹配；不再依据名称含"猫"推断） */
    private String species;
    private BigDecimal price;
    private BigDecimal area;
    private Integer maxPets;
    private String facilities;
    private String description;
    private String photo;
    private Integer status;
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
