package com.petcare.care.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("activity_record")
public class ActivityRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long orderId;
    private Long petId;
    private Integer durationMinutes;
    private String mood;
    private String photos;
    private Long staffId;
    private LocalDateTime recordTime;
    private String remark;
}
