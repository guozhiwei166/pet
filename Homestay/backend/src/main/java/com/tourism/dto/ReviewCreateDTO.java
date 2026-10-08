package com.tourism.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 发表评价请求(用户)
 */
@Data
public class ReviewCreateDTO {

    @NotNull(message = "缺少订单")
    private Long orderId;

    @NotNull(message = "请给出总评分")
    @Min(value = 1, message = "评分最低 1 分")
    @Max(value = 5, message = "评分最高 5 分")
    private Integer rating;

    @NotNull(message = "请给出卫生评分")
    @Min(value = 1)
    @Max(value = 5)
    private Integer hygieneScore;

    @NotNull(message = "请给出服务评分")
    @Min(value = 1)
    @Max(value = 5)
    private Integer serviceScore;

    @NotNull(message = "请给出设施评分")
    @Min(value = 1)
    @Max(value = 5)
    private Integer facilityScore;

    @NotNull(message = "请给出位置评分")
    @Min(value = 1)
    @Max(value = 5)
    private Integer locationScore;

    @Size(max = 1000, message = "评价内容不能超过 1000 字")
    private String content;

    /** 图片 URL 列表 */
    private List<String> images;
}
