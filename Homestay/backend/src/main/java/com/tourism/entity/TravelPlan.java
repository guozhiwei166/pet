package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 个人旅行计划(创新点②)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("travel_plan")
public class TravelPlan extends BaseEntity {

    private Long userId;
    private String planName;
    private Long destinationId;
    private String destinationName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String intro;
    /** ONGOING / FINISHED / CANCELLED */
    private String status;
}
