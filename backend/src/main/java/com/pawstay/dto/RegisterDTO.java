package com.pawstay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterDTO(
        @NotBlank(message = "请输入手机号") @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确") String phone,
        @NotBlank(message = "请输入密码") @Pattern(regexp = "^\\S{6,20}$", message = "密码需为6-20位") String password,
        @NotBlank(message = "请输入昵称") String nickname,
        @NotBlank(message = "请选择角色") String role,          // OWNER / MERCHANT
        String shopName,   // 商家入驻必填
        String address) {
}
