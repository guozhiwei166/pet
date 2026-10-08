package com.petcare.pet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PetDTO(
        Long id,
        @NotBlank(message = "请填写宠物昵称") String name,
        @NotBlank(message = "请选择物种") String species,
        String breed,
        String gender,
        LocalDate birthDate,
        BigDecimal weight,
        Integer sterilized,
        String furColor,
        String photo,
        String temperament,
        String notes,
        List<Long> tagIds) {
}
