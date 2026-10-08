package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.BizException;
import com.tourism.common.PageVO;
import com.tourism.common.ResultCode;
import com.tourism.dto.ResourceQueryDTO;
import com.tourism.entity.HomestayExperience;
import com.tourism.entity.TourismExperience;
import com.tourism.mapper.HomestayExperienceMapper;
import com.tourism.mapper.TourismExperienceMapper;
import com.tourism.service.TourismExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 旅游体验服务实现
 */
@Service
@RequiredArgsConstructor
public class TourismExperienceServiceImpl implements TourismExperienceService {

    private final TourismExperienceMapper experienceMapper;
    private final HomestayExperienceMapper homestayExperienceMapper;

    @Override
    public PageVO<TourismExperience> page(ResourceQueryDTO query) {
        LambdaQueryWrapper<TourismExperience> wrapper = new LambdaQueryWrapper<TourismExperience>()
                .eq(TourismExperience::getStatus, 1);
        if (query.getDestinationId() != null) {
            wrapper.eq(TourismExperience::getDestinationId, query.getDestinationId());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(TourismExperience::getName, kw)
                    .or().like(TourismExperience::getIntro, kw)
                    .or().like(TourismExperience::getTags, kw));
        }
        if ("newest".equals(query.getSort())) {
            wrapper.orderByDesc(TourismExperience::getCreateTime);
        } else {
            wrapper.orderByDesc(TourismExperience::getRating);
        }
        Page<TourismExperience> page = experienceMapper.selectPage(new Page<>(query.getCurrent(), query.getSize()), wrapper);
        return PageVO.of(page);
    }

    @Override
    public TourismExperience detail(Long id) {
        TourismExperience e = experienceMapper.selectById(id);
        if (e == null || e.getStatus() != 1) {
            throw new BizException(ResultCode.NOT_FOUND, "体验不存在或已下线");
        }
        return e;
    }

    @Override
    public List<TourismExperience> listByHomestay(Long homestayId) {
        List<HomestayExperience> links = homestayExperienceMapper.selectList(new LambdaQueryWrapper<HomestayExperience>()
                .eq(HomestayExperience::getHomestayId, homestayId)
                .orderByAsc(HomestayExperience::getSortOrder));
        List<Long> ids = links.stream().map(HomestayExperience::getExperienceId).toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        return experienceMapper.selectList(new LambdaQueryWrapper<TourismExperience>()
                .in(TourismExperience::getId, ids)
                .eq(TourismExperience::getStatus, 1));
    }

    @Override
    public List<TourismExperience> listByDestination(Long destinationId, int limit) {
        return experienceMapper.selectList(new LambdaQueryWrapper<TourismExperience>()
                .eq(TourismExperience::getDestinationId, destinationId)
                .eq(TourismExperience::getStatus, 1)
                .orderByDesc(TourismExperience::getRating)
                .last("limit " + limit));
    }
}
