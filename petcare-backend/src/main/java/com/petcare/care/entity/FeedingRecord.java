package com.petcare.care.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("feeding_record")
public class FeedingRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long orderId;
    private Long petId;
    private String food;
    private String amount;
    private String appetite;
    private Long staffId;
    private LocalDateTime recordTime;
    private String remark;
}
