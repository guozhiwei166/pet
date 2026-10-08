package com.tourism.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 民宿列表查询条件(游客公开)
 */
@Data
public class HomestayQueryDTO {

    @Min(value = 1, message = "页码最小为 1")
    private long current = 1;

    @Min(value = 1, message = "每页条数最小为 1")
    @Max(value = 60, message = "每页条数最大为 60")
    private long size = 12;

    private Long destinationId;
    private String keyword;          // 名称/简介/特色/地址模糊
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String features;         // 特色筛选,如 海景
    /** recommend=推荐排序 rating=评分 price_asc/price_desc=newest=最新 */
    private String sort = "recommend";
}
