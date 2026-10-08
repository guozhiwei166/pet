package com.petcare.boarding.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record OrderCreateDTO(
        @NotNull(message = "请选择宠物") Long petId,
        @NotNull(message = "请选择房型") Long roomTypeId,
        @NotNull(message = "请选择入住日期") LocalDate startDate,
        @NotNull(message = "请选择离店日期") LocalDate endDate,
        List<ServiceChoice> services,
        @NotNull(message = "请填写联系人") String contactName,
        @NotNull(message = "请填写联系电话") String contactPhone,
        String remark,
        Long userCouponId) {

    public record ServiceChoice(Long serviceId, Integer quantity) {
    }
}
