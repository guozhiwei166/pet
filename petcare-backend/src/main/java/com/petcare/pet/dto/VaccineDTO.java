package com.petcare.pet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record VaccineDTO(
        Long id,
        @NotBlank(message = "请填写疫苗名称") String vaccineName,
        @NotNull(message = "请选择接种日期") LocalDate inoculationDate,
        LocalDate expireDate,
        String org,
        String remark) {
}
