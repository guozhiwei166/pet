package com.tourism.vo;

import lombok.Data;

/**
 * 忘记账号-按手机号找回结果
 */
@Data
public class FindUsernameVO {

    private String username;
    private String nickname;
    /** USER / MERCHANT / ADMIN */
    private String role;
    private String roleText;
    /** 头像,便于用户确认是自己账号 */
    private String avatar;
}
