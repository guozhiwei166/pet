package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 民宿-餐饮关联
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("homestay_restaurant")
public class HomestayRestaurant extends BaseEntity {

    private Long homestayId;
    private Long restaurantId;
    private Integer sortOrder;
}
