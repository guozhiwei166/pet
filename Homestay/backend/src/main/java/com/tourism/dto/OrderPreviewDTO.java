package com.tourism.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 价格试算请求(确认页选房型/日期/间数时实时算价,无需联系人等下单信息)
 */
@Data
public class OrderPreviewDTO {

    @NotNull(message = "请选择民宿")
    private Long homestayId;

    @NotNull(message = "请选择房型")
    private Long roomTypeId;

    /** 入住日期 yyyy-MM-dd */
    @NotBlank(message = "请选择入住日期")
    private String checkInDate;

    /** 退房日期 yyyy-MM-dd */
    @NotBlank(message = "请选择退房日期")
    private String checkOutDate;

    @NotNull(message = "请选择房间数量")
    @Min(value = 1, message = "房间数量至少 1")
    @Max(value = 5, message = "单次最多预订 5 间")
    private Integer roomCount;
}
