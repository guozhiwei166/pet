package com.tourism.vo;

import lombok.Data;

import java.util.List;

/**
 * 民宿关联资源 id 汇总(编辑回显用)
 */
@Data
public class HomestayLinksVO {

    private Long homestayId;
    private List<Long> scenicIds;
    private List<Long> experienceIds;
    private List<Long> restaurantIds;
}
