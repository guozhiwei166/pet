package com.petcare.care.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("care_task")
public class CareTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long petId;
    private Long roomId;
    private String taskType;
    private java.time.LocalDate taskDate;
    private String planTime;
    private String title;
    private String content;
    private String status;
    private Long staffId;
    private LocalDateTime doneTime;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
