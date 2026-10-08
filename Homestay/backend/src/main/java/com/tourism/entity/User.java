package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户(普通用户 / 商家账号 / 管理员统一存于此表,role 区分)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`user`")
public class User extends BaseEntity {

    private String username;

    /** 密码不参与序列化,避免泄露给前端 */
    @JsonIgnore
    private String password;

    private String nickname;
    private String phone;
    private String avatar;
    private Integer gender;
    private String email;
    private String role;
    /** 0=正常 1=禁用 */
    private Integer status;
}
