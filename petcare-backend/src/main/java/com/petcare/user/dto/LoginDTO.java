package com.petcare.user.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @NotBlank(message = "请输入账号") String username,
        @NotBlank(message = "请输入密码") String password,
        String captchaId,
        String captchaCode) {
}
