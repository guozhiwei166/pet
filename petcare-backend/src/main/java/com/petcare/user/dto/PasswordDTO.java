package com.petcare.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordDTO(
        @NotBlank(message = "请输入旧密码") String oldPassword,
        @NotBlank(message = "请输入新密码") @Pattern(regexp = "^\\S{6,20}$", message = "新密码需为6-20位") String newPassword,
        String securityAnswer) {
}
