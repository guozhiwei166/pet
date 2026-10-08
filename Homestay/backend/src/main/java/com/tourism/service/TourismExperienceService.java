package com.tourism.service;

import com.tourism.common.PageVO;
import com.tourism.dto.ResourceQueryDTO;
import com.tourism.entity.TourismExperience;

import java.util.List;

/**
 * 旅游体验服务(游客浏览 + 关联;管理端走 Admin)
 */
public interface TourismExperienceService {

    PageVO<TourismExperience> page(ResourceQueryDTO query);

    TourismExperience detail(Long id);

    List<TourismExperience> listByHomestay(Long homestayId);

    List<TourismExperience> listByDestination(Long destinationId, int limit);
}
