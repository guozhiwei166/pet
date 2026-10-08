package com.tourism.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 房型日历库存查询参数(游客)
 */
@Data
public class RoomCalendarQueryDTO {

    @Min(value = 1)
    private long current = 1;

    @Min(value = 1)
    @Max(value = 30)
    private long size = 10;

    private Long homestayId;
    /** 起止日期(可选),用于价格日历 */
    private String startDate;
    private String endDate;
    private Integer maxGuests;
}
