package com.tourism.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 民宿关联资源保存请求
 */
@Data
public class ResourceLinkDTO {

    @NotNull(message = "缺少民宿")
    private Long homestayId;

    private List<Long> targetIds;
}
