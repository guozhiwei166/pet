package com.petcare.user.vo;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String avatar;
    private String role;
    private String roleName;
    /** 密保问题（null/空=未设置） */
    private String securityQuestion;
}
