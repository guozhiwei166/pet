package com.tourism.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录响应 VO:token + 用户基础信息
 */
@Data
public class LoginVO {

    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String role;
    /** 商家是否已通过审核(role=MERCHANT 时有效),商家端据此决定可进入经营页面 */
    private Boolean merchantApproved;
    /** 商家审核状态 */
    private String merchantStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;
}
