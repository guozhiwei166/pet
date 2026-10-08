package com.pawstay.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PetDTO(
        Long id,               // 有 id 为编辑，无 id 为新增
        String name,
        String species,        // CAT / DOG
        String breed,
        String gender,         // M / F
        LocalDate birthDate,
        BigDecimal weight,
        Integer sterilized,
        String temperament,
        Integer aggressive,
        Integer vaccinated,
        String vaccineNote,
        String photo,
        String notes) {
}
