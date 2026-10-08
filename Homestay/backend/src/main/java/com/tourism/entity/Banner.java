package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 首页轮播
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("banner")
public class Banner extends BaseEntity {

    private String title;
    private String imageUrl;
    /** NONE / HOMESTAY / SCENIC */
    private String linkType;
    private Long linkId;
    private Integer sortOrder;
    /** 1=展示 0=隐藏 */
    private Integer status;
}
