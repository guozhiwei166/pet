package com.pawstay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pet_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String phone;
    private String password;
    private String nickname;
    private String avatar;
    private String role;        // OWNER 宠物主人 / MERCHANT 商家 / ADMIN 平台管理员
    private Integer status;     // 1 正常 0 停用
    private LocalDateTime createTime;
}
