package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.BizException;
import com.tourism.common.PageVO;
import com.tourism.common.ResultCode;
import com.tourism.dto.ResourceQueryDTO;
import com.tourism.dto.ResourceSaveDTO;
import com.tourism.entity.Destination;
import com.tourism.entity.Homestay;
import com.tourism.entity.Restaurant;
import com.tourism.entity.ScenicSpot;
import com.tourism.entity.TourismExperience;
import com.tourism.mapper.DestinationMapper;
import com.tourism.mapper.HomestayMapper;
import com.tourism.mapper.RestaurantMapper;
import com.tourism.mapper.ScenicSpotMapper;
import com.tourism.mapper.TourismExperienceMapper;
import com.tourism.service.AdminResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 管理员资源管理实现(目的地/景点/体验/餐饮 CRUD + 上下架)
 */
@Service
@RequiredArgsConstructor
public class AdminResourceServiceImpl implements AdminResourceService {

    private static final int TYPE_SCENIC = 1;
    private static final int TYPE_EXPERIENCE = 2;
    private static final int TYPE_RESTAURANT = 3;
    private static final int TYPE_DESTINATION = 4;

    private final DestinationMapper destinationMapper;
    private final ScenicSpotMapper scenicSpotMapper;
    private final TourismExperienceMapper experienceMapper;
    private final RestaurantMapper restaurantMapper;
    private final HomestayMapper homestayMapper;

    // ==================== 目的地 ====================

    @Override
    public PageVO<Destination> pageDestinations(int current, int size, String keyword) {
        LambdaQueryWrapper<Destination> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Destination::getName, keyword.trim());
        }
        wrapper.orderByAsc(Destination::getSortOrder);
        return PageVO.of(destinationMapper.selectPage(new Page<>(current, size), wrapper));
    }

    @Override
    public Destination saveDestination(ResourceSaveDTO dto) {
        Destination d;
        if (dto.getId() != null) {
            d = destinationMapper.selectById(dto.getId());
            if (d == null) {
                throw new BizException(ResultCode.NOT_FOUND, "目的地不存在");
            }
        } else {
            d = new Destination();
        }
        d.setName(dto.getName());
        d.setProvince(dto.getProvince() == null ? "" : dto.getProvince());
        d.setCity(dto.getCity() == null ? dto.getName() : dto.getCity());
        d.setLevel(dto.getLevel() == null ? 2 : dto.getLevel());
        d.setCoverImage(dto.getCoverImage());
        d.setDescription(dto.getDescription());
        d.setScenicTags(dto.getScenicTags());
        d.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        d.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        if (d.getId() == null) {
            destinationMapper.insert(d);
        } else {
            destinationMapper.updateById(d);
        }
        return d;
    }

    @Override
    @Transactional
    public void deleteDestination(Long id) {
        Long cnt = homestayMapper.selectCount(new LambdaQueryWrapper<Homestay>().eq(Homestay::getDestinationId, id));
        if (cnt > 0) {
            throw new BizException("该目的地已有民宿,无法删除,可改为隐藏");
        }
        destinationMapper.deleteById(id);
    }

    // ==================== 景点 ====================

    @Override
    public PageVO<ScenicSpot> pageScenic(ResourceQueryDTO query, boolean includeOffline) {
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();
        if (!includeOffline) {
            wrapper.eq(ScenicSpot::getStatus, 1);
        }
        if (query.getDestinationId() != null) {
            wrapper.eq(ScenicSpot::getDestinationId, query.getDestinationId());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(ScenicSpot::getName, query.getKeyword().trim());
        }
        wrapper.orderByDesc(ScenicSpot::getCreateTime);
        return PageVO.of(scenicSpotMapper.selectPage(new Page<>(query.getCurrent(), query.getSize()), wrapper));
    }

    @Override
    public ScenicSpot saveScenic(ResourceSaveDTO dto) {
        ScenicSpot s;
        if (dto.getId() != null) {
            s = scenicSpotMapper.selectById(dto.getId());
            if (s == null) {
                throw new BizException(ResultCode.NOT_FOUND, "景点不存在");
            }
        } else {
            s = new ScenicSpot();
        }
        s.setDestinationId(dto.getDestinationId());
        s.setName(dto.getName());
        s.setCoverImage(dto.getCoverImage());
        s.setIntro(dto.getIntro());
        s.setDescription(dto.getDescription());
        s.setAddress(dto.getAddress());
        s.setLongitude(dto.getLongitude());
        s.setLatitude(dto.getLatitude());
        s.setPrice(dto.getPrice());
        s.setTags(dto.getTags());
        s.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        if (s.getId() == null) {
            s.setRating(new java.math.BigDecimal("5.0"));
            s.setFavoriteCount(0);
            scenicSpotMapper.insert(s);
        } else {
            scenicSpotMapper.updateById(s);
        }
        return s;
    }

    @Override
    public void deleteScenic(Long id) {
        scenicSpotMapper.deleteById(id);
    }

    @Override
    public void changeScenicStatus(Long id, Integer status) {
        ScenicSpot s = scenicSpotMapper.selectById(id);
        if (s != null) {
            s.setStatus(status);
            scenicSpotMapper.updateById(s);
        }
    }

    // ==================== 体验 ====================

    @Override
    public PageVO<TourismExperience> pageExperience(ResourceQueryDTO query, boolean includeOffline) {
        LambdaQueryWrapper<TourismExperience> wrapper = new LambdaQueryWrapper<>();
        if (!includeOffline) {
            wrapper.eq(TourismExperience::getStatus, 1);
        }
        if (query.getDestinationId() != null) {
            wrapper.eq(TourismExperience::getDestinationId, query.getDestinationId());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(TourismExperience::getName, query.getKeyword().trim());
        }
        wrapper.orderByDesc(TourismExperience::getCreateTime);
        return PageVO.of(experienceMapper.selectPage(new Page<>(query.getCurrent(), query.getSize()), wrapper));
    }

    @Override
    public TourismExperience saveExperience(ResourceSaveDTO dto) {
        TourismExperience e;
        if (dto.getId() != null) {
            e = experienceMapper.selectById(dto.getId());
            if (e == null) {
                throw new BizException(ResultCode.NOT_FOUND, "体验不存在");
            }
        } else {
            e = new TourismExperience();
        }
        BeanUtils.copyProperties(dto, e, "id", "type", "province", "city", "level", "scenicTags", "sortOrder", "images");
        e.setDestinationId(dto.getDestinationId());
        e.setName(dto.getName());
        e.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        if (e.getId() == null) {
            e.setRating(new java.math.BigDecimal("5.0"));
            e.setFavoriteCount(0);
            experienceMapper.insert(e);
        } else {
            experienceMapper.updateById(e);
        }
        return e;
    }

    @Override
    public void deleteExperience(Long id) {
        experienceMapper.deleteById(id);
    }

    @Override
    public void changeExperienceStatus(Long id, Integer status) {
        TourismExperience e = experienceMapper.selectById(id);
        if (e != null) {
            e.setStatus(status);
            experienceMapper.updateById(e);
        }
    }

    // ==================== 餐饮 ====================

    @Override
    public PageVO<Restaurant> pageRestaurant(ResourceQueryDTO query, boolean includeOffline) {
        LambdaQueryWrapper<Restaurant> wrapper = new LambdaQueryWrapper<>();
        if (!includeOffline) {
            wrapper.eq(Restaurant::getStatus, 1);
        }
        if (query.getDestinationId() != null) {
            wrapper.eq(Restaurant::getDestinationId, query.getDestinationId());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(Restaurant::getName, query.getKeyword().trim());
        }
        wrapper.orderByDesc(Restaurant::getCreateTime);
        return PageVO.of(restaurantMapper.selectPage(new Page<>(query.getCurrent(), query.getSize()), wrapper));
    }

    @Override
    public Restaurant saveRestaurant(ResourceSaveDTO dto) {
        Restaurant r;
        if (dto.getId() != null) {
            r = restaurantMapper.selectById(dto.getId());
            if (r == null) {
                throw new BizException(ResultCode.NOT_FOUND, "餐饮不存在");
            }
        } else {
            r = new Restaurant();
        }
        BeanUtils.copyProperties(dto, r, "id", "type", "province", "city", "level", "scenicTags", "sortOrder", "images");
        r.setDestinationId(dto.getDestinationId());
        r.setName(dto.getName());
        r.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        if (r.getId() == null) {
            r.setRating(new java.math.BigDecimal("5.0"));
            r.setFavoriteCount(0);
            restaurantMapper.insert(r);
        } else {
            restaurantMapper.updateById(r);
        }
        return r;
    }

    @Override
    public void deleteRestaurant(Long id) {
        restaurantMapper.deleteById(id);
    }

    @Override
    public void changeRestaurantStatus(Long id, Integer status) {
        Restaurant r = restaurantMapper.selectById(id);
        if (r != null) {
            r.setStatus(status);
            restaurantMapper.updateById(r);
        }
    }
}
