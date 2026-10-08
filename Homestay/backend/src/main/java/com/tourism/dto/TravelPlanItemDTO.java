package com.tourism.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

/**
 * 添加行程项目请求
 */
@Data
public class TravelPlanItemDTO {

    @NotNull(message = "缺少计划")
    private Long planId;

    @NotNull(message = "请选择第几天")
    @Min(value = 1, message = "天数从 1 开始")
    @Max(value = 30, message = "最多 30 天")
    private Integer dayNumber;

    /** HOMESTAY / SCENIC / EXPERIENCE / RESTAURANT */
    @NotNull(message = "缺少项目类型")
    private String itemType;

    @NotNull(message = "缺少项目")
    private Long itemId;

    private LocalTime startTime;
    private LocalTime endTime;

    private String remark;
}
