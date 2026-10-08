package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 民宿-景点关联
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("homestay_scenic")
public class HomestayScenic extends BaseEntity {

    private Long homestayId;
    private Long scenicId;
    private Integer sortOrder;
}
