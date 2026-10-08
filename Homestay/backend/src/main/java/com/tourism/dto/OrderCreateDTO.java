package com.tourism.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 下单请求(用户)
 */
@Data
public class OrderCreateDTO {

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

    @Size(max = 50)
    private String contactName;

    @NotBlank(message = "联系电话不能为空")
    @Size(max = 20)
    private String contactPhone;

    @Size(max = 255)
    private String guestNames;

    @Size(max = 255)
    private String remark;
}
