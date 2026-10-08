package com.pawstay.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DiaryDTO(
        LocalDate recordDate,   // 缺省为今天
        Integer mealDone,
        Integer walkDone,
        String mood,
        String content,
        String photos,
        BigDecimal weight) {
}
