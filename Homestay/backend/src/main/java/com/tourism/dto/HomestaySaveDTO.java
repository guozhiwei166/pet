package com.tourism.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 民宿保存请求(商家新增/编辑)
 */
@Data
public class HomestaySaveDTO {

    private Long id;

    @NotNull(message = "请选择所属目的地")
    private Long destinationId;

    @NotBlank(message = "民宿名称不能为空")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "请上传封面图")
    private String coverImage;

    /** 图集 URL 列表 */
    private List<String> images;

    @NotBlank(message = "详细地址不能为空")
    private String address;

    @NotNull(message = "请填写经度")
    private BigDecimal longitude;

    @NotNull(message = "请填写纬度")
    private BigDecimal latitude;

    @Size(max = 500)
    private String intro;

    private String description;

    private String features;

    private String facilities;

    private String phone;

    /** OPEN / CLOSED */
    private String businessStatus = "OPEN";
}
