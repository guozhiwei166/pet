package com.petcare.employee.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("employee")
public class Employee {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String empNo;
    private String realName;
    private String avatar;
    private String phone;
    private String position;
    private String area;
    private Integer status;
    private LocalDate hireDate;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
