package com.tourism.service;

import com.tourism.common.PageVO;
import com.tourism.dto.ResourceQueryDTO;
import com.tourism.entity.Restaurant;

import java.util.List;

/**
 * 餐饮服务(游客浏览 + 关联;管理端走 Admin)
 */
public interface RestaurantService {

    PageVO<Restaurant> page(ResourceQueryDTO query);

    Restaurant detail(Long id);

    List<Restaurant> listByHomestay(Long homestayId);

    List<Restaurant> listByDestination(Long destinationId, int limit);
}
