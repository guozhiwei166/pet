package com.tourism.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 房型保存请求(商家)
 */
@Data
public class RoomTypeSaveDTO {

    private Long id;

    @NotNull(message = "请选择所属民宿")
    private Long homestayId;

    @NotBlank(message = "房型名称不能为空")
    @Size(max = 50)
    private String name;

    private String coverImage;

    @Size(max = 50)
    private String bedType;

    @NotNull(message = "请填写可入住人数")
    @Min(value = 1, message = "入住人数至少 1 人")
    @Max(value = 20, message = "入住人数不能超过 20")
    private Integer maxGuests;

    private BigDecimal area;

    /** 0=不含早 1=含早 */
    private Integer hasBreakfast;

    @NotNull(message = "请填写单价")
    @DecimalMin(value = "0.01", message = "单价需大于 0")
    private BigDecimal price;

    @NotNull(message = "请填写房间数量")
    @Min(value = 1, message = "房间数量至少 1 间")
    @Max(value = 200, message = "房间数量不能超过 200")
    private Integer roomCount;

    @Size(max = 1000)
    private String intro;

    private String facilities;

    /** ON / OFF */
    private String status = "ON";
}
