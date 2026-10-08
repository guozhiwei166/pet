package com.pawstay.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record OrderCreateDTO(
        @NotNull(message = "请选择宠物") Long petId,
        @NotNull(message = "请选择房型") Long roomId,
        @NotNull(message = "请选择入住日期") LocalDate startDate,
        @NotNull(message = "请选择离店日期") LocalDate endDate,
        @NotNull(message = "请填写联系人") String contactName,
        @NotNull(message = "请填写联系电话") String contactPhone,
        String remark,
        Boolean agreementSigned) {
}
