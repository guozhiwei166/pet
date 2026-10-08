package com.petcare.store.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 服务预约：美容/洗澡/接送等独立于寄养主订单的预约 */
@Data
@TableName("service_booking")
public class ServiceBooking {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long petId;
    private Long serviceId;
    private String serviceName;
    private BigDecimal price;
    private LocalDate bookDate;
    private String bookTime;
    private String status;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
