package com.tourism.dto;

import lombok.Data;

/**
 * 资源(景点/体验/餐饮)分页查询(游客公开)
 */
@Data
public class ResourceQueryDTO {

    private long current = 1;
    private long size = 12;
    private Long destinationId;
    private String keyword;
    /** rating / newest */
    private String sort = "rating";
}
