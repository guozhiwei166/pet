package com.tourism.service;

import com.tourism.common.PageVO;
import com.tourism.dto.ResourceQueryDTO;
import com.tourism.entity.ScenicSpot;

import java.util.List;

/**
 * 景点服务(游客浏览 + 关联查询;管理端 CRUD 走 Admin)
 */
public interface ScenicSpotService {

    PageVO<ScenicSpot> page(ResourceQueryDTO query);

    ScenicSpot detail(Long id);

    /** 民宿关联的景点(民宿详情"住这里玩什么") */
    List<ScenicSpot> listByHomestay(Long homestayId);

    /** 目的地热门景点(旅行计划选择用) */
    List<ScenicSpot> listByDestination(Long destinationId, int limit);
}
