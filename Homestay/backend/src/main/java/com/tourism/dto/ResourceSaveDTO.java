package com.tourism.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 资源/目的地保存请求(管理员)。四种资源共用,按需传字段。
 */
@Data
public class ResourceSaveDTO {

    private Long id;

    /** 1=景点 2=体验 3=餐饮 4=目的地 */
    @NotNull(message = "缺少资源类型")
    private Integer type;

    private Long destinationId;

    @NotBlank(message = "名称不能为空")
    @Size(max = 100)
    private String name;

    private String coverImage;
    private String images;
    private String intro;
    private String description;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private BigDecimal price;
    private String tags;

    /** 1=上架 0=下架(目的地:1=展示 0=隐藏) */
    private Integer status = 1;

    // 目的地专属
    private String province;
    private String city;
    private Integer level;
    private String scenicTags;
    private Integer sortOrder;
}
