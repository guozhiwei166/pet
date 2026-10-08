package com.tourism.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户管理列表项(管理员)
 */
@Data
public class UserAdminVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String role;
    private String roleText;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 商家相关(role=MERCHANT) */
    private Long merchantId;
    private String merchantName;
    private String merchantStatus;
    private String merchantStatusText;
}
