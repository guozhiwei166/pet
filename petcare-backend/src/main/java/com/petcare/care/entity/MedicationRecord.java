package com.petcare.care.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("medication_record")
public class MedicationRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long orderId;
    private Long petId;
    private String medicine;
    private String dosage;
    private Long staffId;
    private LocalDateTime recordTime;
    private String remark;
}
