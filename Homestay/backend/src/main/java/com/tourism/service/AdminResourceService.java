package com.tourism.service;

import com.tourism.common.PageVO;
import com.tourism.dto.ResourceQueryDTO;
import com.tourism.entity.Destination;
import com.tourism.entity.Restaurant;
import com.tourism.entity.ScenicSpot;
import com.tourism.entity.TourismExperience;
import com.tourism.dto.ResourceSaveDTO;

/**
 * 管理员-旅游资源/目的地管理
 */
public interface AdminResourceService {

    // ===== 目的地管理 =====
    PageVO<Destination> pageDestinations(int current, int size, String keyword);

    Destination saveDestination(ResourceSaveDTO dto);

    void deleteDestination(Long id);

    // ===== 景点 =====
    PageVO<ScenicSpot> pageScenic(ResourceQueryDTO query, boolean includeOffline);

    ScenicSpot saveScenic(ResourceSaveDTO dto);

    void deleteScenic(Long id);

    void changeScenicStatus(Long id, Integer status);

    // ===== 体验 =====
    PageVO<TourismExperience> pageExperience(ResourceQueryDTO query, boolean includeOffline);

    TourismExperience saveExperience(ResourceSaveDTO dto);

    void deleteExperience(Long id);

    void changeExperienceStatus(Long id, Integer status);

    // ===== 餐饮 =====
    PageVO<Restaurant> pageRestaurant(ResourceQueryDTO query, boolean includeOffline);

    Restaurant saveRestaurant(ResourceSaveDTO dto);

    void deleteRestaurant(Long id);

    void changeRestaurantStatus(Long id, Integer status);
}
