package com.petcare.room.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 增值服务（表名 service；Java 类名避开 Spring @Service 注解重名）
 */
@Data
@TableName("service")
public class ServiceItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String photo;
    private BigDecimal price;
    private String unit;
    private String description;
    private Integer status;
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
