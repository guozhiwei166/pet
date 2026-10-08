package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

/**
 * 旅行计划行程项目
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("travel_plan_item")
public class TravelPlanItem extends BaseEntity {

    private Long planId;
    /** 第几天,从 1 开始 */
    private Integer dayNumber;
    /** HOMESTAY / SCENIC / EXPERIENCE / RESTAURANT */
    private String itemType;
    private Long itemId;
    /** 项目名称快照 */
    private String itemName;
    private LocalTime startTime;
    private LocalTime endTime;
    private String remark;
    /** 同天内排序 */
    private Integer sortOrder;
}
