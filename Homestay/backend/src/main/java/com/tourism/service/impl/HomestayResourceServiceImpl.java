package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourism.common.BizException;
import com.tourism.common.ResultCode;
import com.tourism.entity.Homestay;
import com.tourism.entity.HomestayExperience;
import com.tourism.entity.HomestayRestaurant;
import com.tourism.entity.HomestayScenic;
import com.tourism.mapper.HomestayExperienceMapper;
import com.tourism.mapper.HomestayMapper;
import com.tourism.mapper.HomestayRestaurantMapper;
import com.tourism.mapper.HomestayScenicMapper;
import com.tourism.service.HomestayResourceService;
import com.tourism.vo.HomestayLinksVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 民宿-资源关联维护实现
 */
@Service
@RequiredArgsConstructor
public class HomestayResourceServiceImpl implements HomestayResourceService {

    private final HomestayMapper homestayMapper;
    private final HomestayScenicMapper homestayScenicMapper;
    private final HomestayExperienceMapper homestayExperienceMapper;
    private final HomestayRestaurantMapper homestayRestaurantMapper;

    @Override
    @Transactional
    public void saveScenic(Long homestayId, Long merchantId, List<Long> scenicIds) {
        requireOwner(homestayId, merchantId);
        homestayScenicMapper.delete(new LambdaQueryWrapper<HomestayScenic>().eq(HomestayScenic::getHomestayId, homestayId));
        insertIds(scenicIds, id -> {
            HomestayScenic s = new HomestayScenic();
            s.setHomestayId(homestayId);
            s.setScenicId(id);
            homestayScenicMapper.insert(s);
        });
    }

    @Override
    @Transactional
    public void saveExperience(Long homestayId, Long merchantId, List<Long> experienceIds) {
        requireOwner(homestayId, merchantId);
        homestayExperienceMapper.delete(new LambdaQueryWrapper<HomestayExperience>().eq(HomestayExperience::getHomestayId, homestayId));
        insertIds(experienceIds, id -> {
            HomestayExperience e = new HomestayExperience();
            e.setHomestayId(homestayId);
            e.setExperienceId(id);
            homestayExperienceMapper.insert(e);
        });
    }

    @Override
    @Transactional
    public void saveRestaurant(Long homestayId, Long merchantId, List<Long> restaurantIds) {
        requireOwner(homestayId, merchantId);
        homestayRestaurantMapper.delete(new LambdaQueryWrapper<HomestayRestaurant>().eq(HomestayRestaurant::getHomestayId, homestayId));
        insertIds(restaurantIds, id -> {
            HomestayRestaurant r = new HomestayRestaurant();
            r.setHomestayId(homestayId);
            r.setRestaurantId(id);
            homestayRestaurantMapper.insert(r);
        });
    }

    @Override
    public HomestayLinksVO linkIds(Long homestayId) {
        HomestayLinksVO vo = new HomestayLinksVO();
        vo.setHomestayId(homestayId);
        vo.setScenicIds(homestayScenicMapper.selectList(new LambdaQueryWrapper<HomestayScenic>()
                        .eq(HomestayScenic::getHomestayId, homestayId))
                .stream().map(HomestayScenic::getScenicId).toList());
        vo.setExperienceIds(homestayExperienceMapper.selectList(new LambdaQueryWrapper<HomestayExperience>()
                        .eq(HomestayExperience::getHomestayId, homestayId))
                .stream().map(HomestayExperience::getExperienceId).toList());
        vo.setRestaurantIds(homestayRestaurantMapper.selectList(new LambdaQueryWrapper<HomestayRestaurant>()
                        .eq(HomestayRestaurant::getHomestayId, homestayId))
                .stream().map(HomestayRestaurant::getRestaurantId).toList());
        return vo;
    }

    private void requireOwner(Long homestayId, Long merchantId) {
        Homestay h = homestayMapper.selectById(homestayId);
        if (h == null) {
            throw new BizException(ResultCode.NOT_FOUND, "民宿不存在");
        }
        if (!Objects.equals(h.getMerchantId(), merchantId)) {
            throw new BizException(ResultCode.FORBIDDEN, "无权操作该民宿");
        }
    }

    private void insertIds(List<Long> ids, java.util.function.Consumer<Long> consumer) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 去重
        ids.stream().distinct().filter(Objects::nonNull).forEach(consumer);
    }
}
