package com.petcare.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ProfileDTO(
        @NotBlank(message = "昵称不能为空") String nickname,
        String avatar) {
}
