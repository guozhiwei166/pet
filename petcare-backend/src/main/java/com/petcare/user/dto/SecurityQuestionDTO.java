package com.petcare.user.dto;

import jakarta.validation.constraints.NotBlank;

/** 设置/修改密保问题 */
public record SecurityQuestionDTO(
        @NotBlank(message = "请选择密保问题") String question,
        @NotBlank(message = "请输入密保答案") String answer) {
}
