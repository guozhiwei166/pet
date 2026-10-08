package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.BizException;
import com.tourism.common.PageVO;
import com.tourism.common.ResultCode;
import com.tourism.dto.ResourceQueryDTO;
import com.tourism.entity.HomestayScenic;
import com.tourism.entity.ScenicSpot;
import com.tourism.mapper.HomestayScenicMapper;
import com.tourism.mapper.ScenicSpotMapper;
import com.tourism.service.ScenicSpotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 景点服务实现
 */
@Service
@RequiredArgsConstructor
public class ScenicSpotServiceImpl implements ScenicSpotService {

    private final ScenicSpotMapper scenicSpotMapper;
    private final HomestayScenicMapper homestayScenicMapper;

    @Override
    public PageVO<ScenicSpot> page(ResourceQueryDTO query) {
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<ScenicSpot>()
                .eq(ScenicSpot::getStatus, 1);
        if (query.getDestinationId() != null) {
            wrapper.eq(ScenicSpot::getDestinationId, query.getDestinationId());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(ScenicSpot::getName, kw)
                    .or().like(ScenicSpot::getIntro, kw)
                    .or().like(ScenicSpot::getTags, kw));
        }
        if ("newest".equals(query.getSort())) {
            wrapper.orderByDesc(ScenicSpot::getCreateTime);
        } else {
            wrapper.orderByDesc(ScenicSpot::getRating);
        }
        Page<ScenicSpot> page = scenicSpotMapper.selectPage(new Page<>(query.getCurrent(), query.getSize()), wrapper);
        return PageVO.of(page);
    }

    @Override
    public ScenicSpot detail(Long id) {
        ScenicSpot s = scenicSpotMapper.selectById(id);
        if (s == null || s.getStatus() != 1) {
            throw new BizException(ResultCode.NOT_FOUND, "景点不存在或已下线");
        }
        return s;
    }

    @Override
    public List<ScenicSpot> listByHomestay(Long homestayId) {
        List<HomestayScenic> links = homestayScenicMapper.selectList(new LambdaQueryWrapper<HomestayScenic>()
                .eq(HomestayScenic::getHomestayId, homestayId)
                .orderByAsc(HomestayScenic::getSortOrder));
        List<Long> ids = links.stream().map(HomestayScenic::getScenicId).toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        return scenicSpotMapper.selectList(new LambdaQueryWrapper<ScenicSpot>()
                .in(ScenicSpot::getId, ids)
                .eq(ScenicSpot::getStatus, 1));
    }

    @Override
    public List<ScenicSpot> listByDestination(Long destinationId, int limit) {
        return scenicSpotMapper.selectList(new LambdaQueryWrapper<ScenicSpot>()
                .eq(ScenicSpot::getDestinationId, destinationId)
                .eq(ScenicSpot::getStatus, 1)
                .orderByDesc(ScenicSpot::getRating)
                .last("limit " + limit));
    }
}
