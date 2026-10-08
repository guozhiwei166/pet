package com.tourism.service;

import com.tourism.vo.RecommendVO;

import java.util.List;

/**
 * 民宿推荐服务(创新点③:多指标加权可解释)
 * 推荐分 = 用户评分40% + 价格匹配20% + 距离匹配20% + 热门程度10% + 偏好匹配10%
 */
public interface RecommendService {

    /**
     * 获取推荐民宿列表(按综合分降序,附因子解释)
     *
     * @param limit         返回条数
     * @param destinationId 目的地筛选(可选)
     * @param userId        当前用户(可选,游客为 null → 偏好因子取中性)
     * @param maxPrice      用户预算上限(可选)
     */
    List<RecommendVO> recommend(int limit, Long destinationId, Long userId, java.math.BigDecimal maxPrice);

    /** 首页"猜你喜欢":游客(无偏好)默认推荐 */
    List<RecommendVO> recommendForHome(int limit);
}
