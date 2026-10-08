package com.petcare.care.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cleaning_record")
public class CleaningRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long roomId;
    private Long orderId;
    private Long staffId;
    private LocalDateTime recordTime;
}
