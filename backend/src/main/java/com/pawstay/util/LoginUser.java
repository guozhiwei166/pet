package com.pawstay.util;

import lombok.Data;

@Data
public class LoginUser {
    private Long id;
    private String role;

    public static LoginUser of(Long id, String role) {
        LoginUser u = new LoginUser();
        u.setId(id);
        u.setRole(role);
        return u;
    }
}
