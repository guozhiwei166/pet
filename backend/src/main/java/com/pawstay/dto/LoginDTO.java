package com.pawstay.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @NotBlank(message = "请输入手机号") String phone,
        @NotBlank(message = "请输入密码") String password) {
}
