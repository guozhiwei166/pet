package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourism.common.BizException;
import com.tourism.common.Constants;
import com.tourism.entity.Destination;
import com.tourism.entity.Favorite;
import com.tourism.entity.Homestay;
import com.tourism.entity.Restaurant;
import com.tourism.entity.ScenicSpot;
import com.tourism.entity.TourismExperience;
import com.tourism.common.PageVO;
import com.tourism.mapper.DestinationMapper;
import com.tourism.mapper.FavoriteMapper;
import com.tourism.mapper.HomestayMapper;
import com.tourism.mapper.RestaurantMapper;
import com.tourism.mapper.ScenicSpotMapper;
import com.tourism.mapper.TourismExperienceMapper;
import com.tourism.service.FavoriteService;
import com.tourism.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 收藏服务实现(收藏表物理删除)
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final HomestayMapper homestayMapper;
    private final ScenicSpotMapper scenicSpotMapper;
    private final TourismExperienceMapper experienceMapper;
    private final RestaurantMapper restaurantMapper;
    private final DestinationMapper destinationMapper;

    private static final Map<Integer, String> TYPE_TEXT = Map.of(
            Constants.TARGET_HOMESTAY, "民宿",
            Constants.TARGET_SCENIC, "景点",
            Constants.TARGET_EXPERIENCE, "体验",
            Constants.TARGET_RESTAURANT, "餐饮"
    );

    @Override
    @Transactional
    public boolean toggle(Long userId, int targetType, Long targetId) {
        requireTargetExists(targetType, targetId);
        Favorite exist = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId)
                .last("limit 1"));
        if (exist != null) {
            favoriteMapper.deleteById(exist.getId());
            changeFavoriteCount(targetType, targetId, -1);
            return false;
        }
        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setTargetType(targetType);
        f.setTargetId(targetId);
        f.setCreateTime(LocalDateTime.now());
        favoriteMapper.insert(f);
        changeFavoriteCount(targetType, targetId, 1);
        return true;
    }

    @Override
    @Transactional
    public void remove(Long userId, int targetType, Long targetId) {
        Favorite exist = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId)
                .last("limit 1"));
        if (exist != null) {
            favoriteMapper.deleteById(exist.getId());
            changeFavoriteCount(targetType, targetId, -1);
        }
        // 未收藏时静默成功,幂等删除
    }

    @Override
    public boolean isFavorited(Long userId, int targetType, Long targetId) {
        return favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId)) > 0;
    }

    @Override
    public PageVO<FavoriteVO> page(Long userId, int current, int size, Integer targetType) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime);
        if (targetType != null) {
            wrapper.eq(Favorite::getTargetType, targetType);
        }
        var page = favoriteMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size), wrapper);

        PageVO<FavoriteVO> result = new PageVO<>();
        result.setRecords(page.getRecords().stream().map(this::toVO).toList());
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setPages(page.getPages());
        return result;
    }

    // ==================== 内部 ====================

    /** 目标不存在(含下架)不可收藏 */
    private void requireTargetExists(int targetType, Long targetId) {
        switch (targetType) {
            case Constants.TARGET_HOMESTAY -> {
                Homestay h = homestayMapper.selectById(targetId);
                if (h == null || !Constants.AUDIT_APPROVED.equals(h.getAuditStatus())) {
                    throw new BizException("民宿不存在或未上架");
                }
            }
            case Constants.TARGET_SCENIC -> {
                if (scenicSpotMapper.selectById(targetId) == null) {
                    throw new BizException("景点不存在");
                }
            }
            case Constants.TARGET_EXPERIENCE -> {
                if (experienceMapper.selectById(targetId) == null) {
                    throw new BizException("体验不存在");
                }
            }
            case Constants.TARGET_RESTAURANT -> {
                if (restaurantMapper.selectById(targetId) == null) {
                    throw new BizException("餐饮不存在");
                }
            }
            default -> throw new BizException("收藏类型不正确");
        }
    }

    private void changeFavoriteCount(int targetType, Long targetId, int delta) {
        switch (targetType) {
            case Constants.TARGET_HOMESTAY -> {
                Homestay h = homestayMapper.selectById(targetId);
                if (h != null) {
                    h.setFavoriteCount(Math.max(0, h.getFavoriteCount() + delta));
                    homestayMapper.updateById(h);
                }
            }
            case Constants.TARGET_SCENIC -> {
                ScenicSpot s = scenicSpotMapper.selectById(targetId);
                if (s != null) {
                    s.setFavoriteCount(Math.max(0, s.getFavoriteCount() + delta));
                    scenicSpotMapper.updateById(s);
                }
            }
            case Constants.TARGET_EXPERIENCE -> {
                TourismExperience e = experienceMapper.selectById(targetId);
                if (e != null) {
                    e.setFavoriteCount(Math.max(0, e.getFavoriteCount() + delta));
                    experienceMapper.updateById(e);
                }
            }
            case Constants.TARGET_RESTAURANT -> {
                Restaurant r = restaurantMapper.selectById(targetId);
                if (r != null) {
                    r.setFavoriteCount(Math.max(0, r.getFavoriteCount() + delta));
                    restaurantMapper.updateById(r);
                }
            }
            default -> {
            }
        }
    }

    private FavoriteVO toVO(Favorite f) {
        FavoriteVO vo = new FavoriteVO();
        vo.setFavoriteId(f.getId());
        vo.setTargetId(f.getTargetId());
        vo.setTargetType(f.getTargetType());
        vo.setTypeText(TYPE_TEXT.getOrDefault(f.getTargetType(), ""));
        vo.setFavoriteTime(f.getCreateTime());

        switch (f.getTargetType()) {
            case Constants.TARGET_HOMESTAY -> {
                Homestay h = homestayMapper.selectById(f.getTargetId());
                if (h != null) {
                    vo.setName(h.getName());
                    vo.setCoverImage(h.getCoverImage());
                    vo.setRating(h.getRating());
                    vo.setPrice(h.getMinPrice());
                    vo.setIntro(h.getIntro());
                    Destination d = destinationMapper.selectById(h.getDestinationId());
                    if (d != null) vo.setDestinationName(d.getName());
                }
            }
            case Constants.TARGET_SCENIC -> copyScenic(scenicSpotMapper.selectById(f.getTargetId()), vo);
            case Constants.TARGET_EXPERIENCE -> {
                TourismExperience e = experienceMapper.selectById(f.getTargetId());
                if (e != null) {
                    vo.setName(e.getName());
                    vo.setCoverImage(e.getCoverImage());
                    vo.setRating(e.getRating());
                    vo.setPrice(e.getPrice());
                    vo.setIntro(e.getIntro());
                    Destination d = destinationMapper.selectById(e.getDestinationId());
                    if (d != null) vo.setDestinationName(d.getName());
                }
            }
            case Constants.TARGET_RESTAURANT -> {
                Restaurant r = restaurantMapper.selectById(f.getTargetId());
                if (r != null) {
                    vo.setName(r.getName());
                    vo.setCoverImage(r.getCoverImage());
                    vo.setRating(r.getRating());
                    vo.setPrice(r.getPrice());
                    vo.setIntro(r.getIntro());
                    Destination d = destinationMapper.selectById(r.getDestinationId());
                    if (d != null) vo.setDestinationName(d.getName());
                }
            }
            default -> {
            }
        }
        return vo;
    }

    private void copyScenic(ScenicSpot s, FavoriteVO vo) {
        if (s != null) {
            vo.setName(s.getName());
            vo.setCoverImage(s.getCoverImage());
            vo.setRating(s.getRating());
            vo.setPrice(s.getPrice());
            vo.setIntro(s.getIntro());
            Destination d = destinationMapper.selectById(s.getDestinationId());
            if (d != null) vo.setDestinationName(d.getName());
        }
    }
}
