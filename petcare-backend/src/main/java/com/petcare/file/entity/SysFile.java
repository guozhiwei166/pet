package com.petcare.file.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_file")
public class SysFile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String storageName;
    private String originalName;
    private String path;
    private String bizType;
    private Long size;
    private String contentType;
    private Long uploadBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
