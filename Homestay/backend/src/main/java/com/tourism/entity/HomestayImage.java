package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 民宿多图
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("homestay_image")
public class HomestayImage extends BaseEntity {

    private Long homestayId;
    private String url;
    private Integer sortOrder;
}
