package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourism.common.BizException;
import com.tourism.common.Constants;
import com.tourism.entity.Destination;
import com.tourism.entity.Homestay;
import com.tourism.mapper.DestinationMapper;
import com.tourism.mapper.HomestayMapper;
import com.tourism.mapper.RestaurantMapper;
import com.tourism.mapper.ScenicSpotMapper;
import com.tourism.mapper.TourismExperienceMapper;
import com.tourism.service.DestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 目的地服务实现
 */
@Service
@RequiredArgsConstructor
public class DestinationServiceImpl implements DestinationService {

    private final DestinationMapper destinationMapper;
    private final HomestayMapper homestayMapper;
    private final ScenicSpotMapper scenicSpotMapper;
    private final TourismExperienceMapper experienceMapper;
    private final RestaurantMapper restaurantMapper;

    @Override
    public List<Destination> listShown() {
        return destinationMapper.selectList(new LambdaQueryWrapper<Destination>()
                .eq(Destination::getStatus, 1)
                .eq(Destination::getDeleted, 0)
                .orderByAsc(Destination::getSortOrder)
                .orderByAsc(Destination::getId));
    }

    @Override
    public List<Destination> hotDestinations(int limit) {
        return destinationMapper.selectList(new LambdaQueryWrapper<Destination>()
                .eq(Destination::getStatus, 1)
                .eq(Destination::getDeleted, 0)
                .orderByDesc(Destination::getSortOrder)
                .last("limit " + limit));
    }

    @Override
    public Destination detail(Long id) {
        Destination d = destinationMapper.selectById(id);
        if (d == null) {
            throw new BizException("目的地不存在");
        }
        return d;
    }

    /** 供资源/民宿绑定目的地时校验 */
    public Destination requireExist(Long id) {
        Destination d = destinationMapper.selectById(id);
        if (d == null) {
            throw new BizException("目的地不存在");
        }
        return d;
    }
}
