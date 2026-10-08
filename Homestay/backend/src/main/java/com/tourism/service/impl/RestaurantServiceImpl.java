package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.BizException;
import com.tourism.common.PageVO;
import com.tourism.common.ResultCode;
import com.tourism.dto.ResourceQueryDTO;
import com.tourism.entity.HomestayRestaurant;
import com.tourism.entity.Restaurant;
import com.tourism.mapper.HomestayRestaurantMapper;
import com.tourism.mapper.RestaurantMapper;
import com.tourism.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 餐饮服务实现
 */
@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantMapper restaurantMapper;
    private final HomestayRestaurantMapper homestayRestaurantMapper;

    @Override
    public PageVO<Restaurant> page(ResourceQueryDTO query) {
        LambdaQueryWrapper<Restaurant> wrapper = new LambdaQueryWrapper<Restaurant>()
                .eq(Restaurant::getStatus, 1);
        if (query.getDestinationId() != null) {
            wrapper.eq(Restaurant::getDestinationId, query.getDestinationId());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(Restaurant::getName, kw)
                    .or().like(Restaurant::getIntro, kw)
                    .or().like(Restaurant::getTags, kw));
        }
        if ("newest".equals(query.getSort())) {
            wrapper.orderByDesc(Restaurant::getCreateTime);
        } else {
            wrapper.orderByDesc(Restaurant::getRating);
        }
        Page<Restaurant> page = restaurantMapper.selectPage(new Page<>(query.getCurrent(), query.getSize()), wrapper);
        return PageVO.of(page);
    }

    @Override
    public Restaurant detail(Long id) {
        Restaurant r = restaurantMapper.selectById(id);
        if (r == null || r.getStatus() != 1) {
            throw new BizException(ResultCode.NOT_FOUND, "餐饮不存在或已下线");
        }
        return r;
    }

    @Override
    public List<Restaurant> listByHomestay(Long homestayId) {
        List<HomestayRestaurant> links = homestayRestaurantMapper.selectList(new LambdaQueryWrapper<HomestayRestaurant>()
                .eq(HomestayRestaurant::getHomestayId, homestayId)
                .orderByAsc(HomestayRestaurant::getSortOrder));
        List<Long> ids = links.stream().map(HomestayRestaurant::getRestaurantId).toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        return restaurantMapper.selectList(new LambdaQueryWrapper<Restaurant>()
                .in(Restaurant::getId, ids)
                .eq(Restaurant::getStatus, 1));
    }

    @Override
    public List<Restaurant> listByDestination(Long destinationId, int limit) {
        return restaurantMapper.selectList(new LambdaQueryWrapper<Restaurant>()
                .eq(Restaurant::getDestinationId, destinationId)
                .eq(Restaurant::getStatus, 1)
                .orderByDesc(Restaurant::getRating)
                .last("limit " + limit));
    }
}
