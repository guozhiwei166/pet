package com.tourism.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 旅行计划保存请求
 */
@Data
public class TravelPlanDTO {

    private Long id;

    @NotBlank(message = "计划名称不能为空")
    @Size(max = 100)
    private String planName;

    @NotNull(message = "请选择目的地")
    private Long destinationId;

    /** 可选开始/结束日期 yyyy-MM-dd */
    private LocalDate startDate;
    private LocalDate endDate;

    @Size(max = 500)
    private String intro;
}
