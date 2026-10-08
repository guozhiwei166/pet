package com.pawstay.dto;

import java.math.BigDecimal;

public record RoomDTO(
        Long id,
        String name,
        String species,          // CAT / DOG / ALL
        BigDecimal maxWeight,
        Integer needSterilized,
        Integer allowAggressive,
        Integer totalCount,
        BigDecimal pricePerDay,
        String photo,
        String tags,
        String description) {
}
