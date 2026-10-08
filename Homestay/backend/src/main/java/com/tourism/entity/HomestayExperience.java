package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 民宿-旅游体验关联
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("homestay_experience")
public class HomestayExperience extends BaseEntity {

    private Long homestayId;
    private Long experienceId;
    private Integer sortOrder;
}
