package com.tourism.service;

import com.tourism.vo.MerchantStatsVO;
import com.tourism.vo.PlatformStatsVO;

/**
 * 数据统计服务
 */
public interface StatisticsService {

    /** 商家经营统计 */
    MerchantStatsVO merchantStats(Long merchantId);

    /** 平台统计(管理员) */
    PlatformStatsVO platformStats();
}
