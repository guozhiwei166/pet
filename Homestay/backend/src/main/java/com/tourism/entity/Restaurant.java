package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 餐饮
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("restaurant")
public class Restaurant extends BaseEntity {

    private Long destinationId;
    private String name;
    private String coverImage;
    private String images;
    private String intro;
    private String description;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private BigDecimal price;
    private BigDecimal rating;
    private String tags;
    private Integer favoriteCount;
    /** 1=上架 0=下架 */
    private Integer status;
}
