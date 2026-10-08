package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置(键值对,物理删除无意义,故不用逻辑删除)
 */
@Data
@TableName("sys_setting")
public class SysSetting implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String settingKey;
    private String settingValue;
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
