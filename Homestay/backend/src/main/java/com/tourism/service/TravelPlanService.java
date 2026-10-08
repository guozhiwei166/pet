package com.tourism.service;

import com.tourism.common.PageVO;
import com.tourism.dto.TravelPlanDTO;
import com.tourism.dto.TravelPlanItemDTO;
import com.tourism.entity.TravelPlan;
import com.tourism.vo.TravelPlanDetailVO;

import java.util.List;

/**
 * 个人旅行计划服务(创新点②)
 */
public interface TravelPlanService {

    /** 我的计划列表 */
    PageVO<TravelPlan> pageByUser(Long userId, int current, int size);

    /** 新建计划 */
    TravelPlan create(Long userId, TravelPlanDTO dto);

    /** 编辑计划 */
    TravelPlan update(Long userId, TravelPlanDTO dto);

    /** 删除计划(级联删项目) */
    void delete(Long userId, Long planId);

    /** 计划详情(按天分组) */
    TravelPlanDetailVO detail(Long userId, Long planId);

    /** 添加行程项目(校验资源存在) */
    TravelPlanDetailVO addItem(Long userId, TravelPlanItemDTO dto);

    /** 删除行程项目 */
    TravelPlanDetailVO removeItem(Long userId, Long itemId);

    /** 调整项目顺序/天数/时间 */
    TravelPlanDetailVO moveItem(Long userId, Long itemId, Integer dayNumber, Integer sortOrder);

    /** 改变计划状态 */
    void changeStatus(Long userId, Long planId, String status);
}
