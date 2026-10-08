package com.tourism.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息 VO
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String avatar;
    private Integer gender;
    private String email;
    private String role;
    private Integer status;
    /** 商家审核状态(仅商家角色返回) */
    private String merchantStatus;
    /** 商家资料 id(仅商家角色返回) */
    private Long merchantId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
