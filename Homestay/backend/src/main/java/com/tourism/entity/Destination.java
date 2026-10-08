package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 旅游目的地
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("destination")
public class Destination extends BaseEntity {

    private String name;
    private String province;
    private String city;
    /** 1=省 2=城市/地区 3=区县 */
    private Integer level;
    private String coverImage;
    private String description;
    private String scenicTags;
    /** 1=展示 0=隐藏 */
    private Integer status;
    private Integer sortOrder;
}
