package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourism.common.Constants;
import com.tourism.entity.Destination;
import com.tourism.entity.Favorite;
import com.tourism.entity.Homestay;
import com.tourism.mapper.DestinationMapper;
import com.tourism.mapper.FavoriteMapper;
import com.tourism.mapper.HomestayMapper;
import com.tourism.service.RecommendService;
import com.tourism.vo.RecommendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 民宿推荐服务实现
 * 算法:加权评分(可解释、无机器学习)
 *   score = 用户评分*0.4 + 价格匹配*0.2 + 距离匹配*0.2 + 热门程度*0.1 + 偏好匹配*0.1
 * 各因子 0-100,权重合计 1.0,返回每个民宿的因子明细供前端解释"为什么推荐"
 */
@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    /** 距离参考点兜底:大理古城坐标(候选民宿均无经纬度时使用) */
    private static final double REF_LON = 100.2676;
    private static final double REF_LAT = 25.6065;
    private static final double DEFAULT_BUDGET = 800.0;

    private static final BigDecimal W_RATING = new BigDecimal("0.40");
    private static final BigDecimal W_PRICE = new BigDecimal("0.20");
    private static final BigDecimal W_DISTANCE = new BigDecimal("0.20");
    private static final BigDecimal W_HOT = new BigDecimal("0.10");
    private static final BigDecimal W_PREF = new BigDecimal("0.10");

    private final HomestayMapper homestayMapper;
    private final DestinationMapper destinationMapper;
    private final FavoriteMapper favoriteMapper;

    @Override
    public List<RecommendVO> recommend(int limit, Long destinationId, Long userId, BigDecimal maxPrice) {
        LambdaQueryWrapper<Homestay> wrapper = new LambdaQueryWrapper<Homestay>()
                .eq(Homestay::getAuditStatus, Constants.AUDIT_APPROVED)
                .eq(Homestay::getBusinessStatus, Constants.BIZ_OPEN)
                .eq(Homestay::getDeleted, 0);
        if (destinationId != null) {
            wrapper.eq(Homestay::getDestinationId, destinationId);
        }
        List<Homestay> homestays = homestayMapper.selectList(wrapper);
        if (homestays.isEmpty()) {
            return List.of();
        }

        double budget = maxPrice == null || maxPrice.signum() <= 0 ? DEFAULT_BUDGET : maxPrice.doubleValue();
        Set<Long> likedDestinations = likedHomestayDestinations(userId);

        // 距离参考点 = 候选民宿集合的地理质心(有坐标者求均值),使"距离匹配"因子与候选范围一致,
        // 避免固定参考点导致远处目的地民宿的距离分整体触底
        double[] center = centroid(homestays);

        List<RecommendVO> result = new ArrayList<>();
        for (Homestay h : homestays) {
            result.add(scoreHomestay(h, likedDestinations, budget, center));
        }
        result.sort(Comparator.comparing(RecommendVO::getScore).reversed());
        return result.stream().limit(limit).toList();
    }

    /** 候选民宿坐标质心;无有效坐标时回退默认参考点 */
    private double[] centroid(List<Homestay> list) {
        double sumLat = 0, sumLon = 0;
        int n = 0;
        for (Homestay h : list) {
            if (h.getLatitude() != null && h.getLongitude() != null) {
                sumLat += h.getLatitude().doubleValue();
                sumLon += h.getLongitude().doubleValue();
                n++;
            }
        }
        if (n == 0) {
            return new double[]{REF_LAT, REF_LON};
        }
        return new double[]{sumLat / n, sumLon / n};
    }

    @Override
    public List<RecommendVO> recommendForHome(int limit) {
        return recommend(limit, null, null, null);
    }

    // ==================== 核心算法 ====================

    private RecommendVO scoreHomestay(Homestay h, Set<Long> likedDestinations, double budget, double[] center) {
        List<RecommendVO.Factor> factors = new ArrayList<>();
        double sum = 0;

        // 1. 用户评分(0-5 → 0-100)权重 0.4
        double ratingScore = h.getRating().doubleValue() / 5.0 * 100;
        sum += ratingScore * W_RATING.doubleValue();
        factors.add(factor("用户评分", "口碑越好推荐分越高", W_RATING, ratingScore,
                String.format("评分 %.1f/5 → %.0f 分", h.getRating(), ratingScore)));

        // 2. 价格匹配(参考预算),权重 0.2
        double price = h.getMinPrice() == null ? 0 : h.getMinPrice().doubleValue();
        double priceScore;
        if (price <= 0) {
            priceScore = 60;
        } else if (price <= budget) {
            priceScore = 70 + Math.min(30, price / budget * 30);
        } else {
            priceScore = Math.max(20, 70 - Math.min(1.0, (price - budget) / budget) * 50);
        }
        sum += priceScore * W_PRICE.doubleValue();
        factors.add(factor("价格匹配", "参考预算 ¥" + (int) budget, W_PRICE, priceScore,
                String.format("¥%.0f/晚 %s预算", price, price <= budget ? "在预算内" : "略超预算")));

        // 3. 距离匹配(与候选目的地中心越近越高),权重 0.2
        double dist = distance(h.getLatitude() == null ? center[0] : h.getLatitude().doubleValue(),
                h.getLongitude() == null ? center[1] : h.getLongitude().doubleValue(), center[0], center[1]);
        double distScore = Math.max(40, 100 - Math.min(100, dist / 10.0 * 10));
        sum += distScore * W_DISTANCE.doubleValue();
        factors.add(factor("距离匹配", "距目的地中心越近,游玩出行越方便", W_DISTANCE, distScore,
                "距中心约 " + fmtDist(dist)));

        // 4. 热门程度(浏览+收藏归一),权重 0.1
        double hotScore = Math.min(100, h.getViewCount() * 2.0) * 0.5
                + Math.min(100, h.getFavoriteCount() * 10.0) * 0.5;
        sum += hotScore * W_HOT.doubleValue();
        factors.add(factor("热门程度", "浏览与收藏越多人气越高", W_HOT, hotScore,
                "浏览 " + h.getViewCount() + " · 收藏 " + h.getFavoriteCount()));

        // 5. 偏好匹配(用户收藏过民宿的目的地),权重 0.1
        double prefScore = 60;
        String prefExplain = "暂无偏好数据,按中性计分";
        if (likedDestinations != null && likedDestinations.contains(h.getDestinationId())) {
            prefScore = 100;
            prefExplain = "你收藏过该目的地附近的民宿,加分";
        }
        sum += prefScore * W_PREF.doubleValue();
        factors.add(factor("偏好匹配", "基于你的收藏记录", W_PREF, prefScore, prefExplain));

        RecommendVO vo = new RecommendVO();
        vo.setId(h.getId());
        vo.setName(h.getName());
        vo.setCoverImage(h.getCoverImage());
        vo.setIntro(h.getIntro());
        vo.setMinPrice(h.getMinPrice());
        vo.setRating(h.getRating());
        vo.setRatingCount(h.getRatingCount());
        vo.setFavoriteCount(h.getFavoriteCount());
        vo.setFeatures(h.getFeatures());
        Destination d = destinationMapper.selectById(h.getDestinationId());
        if (d != null) {
            vo.setDestinationName(d.getName());
        }
        vo.setScore(BigDecimal.valueOf(sum).setScale(1, RoundingMode.HALF_UP));
        vo.setFactors(factors);
        return vo;
    }

    private RecommendVO.Factor factor(String name, String description, BigDecimal weight,
                                      double score, String explain) {
        RecommendVO.Factor f = new RecommendVO.Factor();
        f.setName(name);
        f.setDescription(description);
        f.setWeight(weight);
        f.setScore(BigDecimal.valueOf(score).setScale(1, RoundingMode.HALF_UP));
        f.setExplain(explain);
        return f;
    }

    /** 用户收藏过的民宿 → 目的地 id 集合(无收藏返回 null) */
    private Set<Long> likedHomestayDestinations(Long userId) {
        if (userId == null) {
            return null;
        }
        List<Favorite> favs = favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, Constants.TARGET_HOMESTAY));
        Set<Long> ids = new HashSet<>();
        for (Favorite f : favs) {
            Homestay h = homestayMapper.selectById(f.getTargetId());
            if (h != null) {
                ids.add(h.getDestinationId());
            }
        }
        return ids.isEmpty() ? null : ids;
    }

    /** Haversine 球面距离(km) */
    private double distance(double lat1, double lon1, double refLat, double refLon) {
        double rad = Math.PI / 180.0;
        double dLat = (refLat - lat1) * rad;
        double dLon = (refLon - lon1) * rad;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1 * rad) * Math.cos(refLat * rad)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 6371.0 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private String fmtDist(double km) {
        return km < 1 ? (int) (km * 1000) + "m" : String.format("%.1fkm", km);
    }
}
