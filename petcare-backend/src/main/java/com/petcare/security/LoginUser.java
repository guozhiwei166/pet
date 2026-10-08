package com.petcare.security;

import lombok.Data;

/**
 * JWT 解析出的登录主体，存入 SecurityContext
 */
@Data
public class LoginUser {
    private Long id;
    private String username;
    private String role;

    public LoginUser(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }
}
