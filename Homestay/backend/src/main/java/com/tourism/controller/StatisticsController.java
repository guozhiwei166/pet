package com.tourism.controller;

import com.tourism.common.Result;
import com.tourism.security.LoginUser;
import com.tourism.security.RequireRole;
import com.tourism.service.MerchantService;
import com.tourism.service.StatisticsService;
import com.tourism.vo.MerchantStatsVO;
import com.tourism.vo.PlatformStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计接口(商家经营统计 + 平台统计)
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final MerchantService merchantService;

    /** 商家经营统计 */
    @GetMapping("/merchant")
    public Result<MerchantStatsVO> merchant() {
        LoginUser.requireRole("MERCHANT");
        merchantService.requireApproved();
        return Result.success(statisticsService.merchantStats(merchantService.currentMerchant().getId()));
    }

    /** 平台统计(管理员) */
    @RequireRole("ADMIN")
    @GetMapping("/platform")
    public Result<PlatformStatsVO> platform() {
        return Result.success(statisticsService.platformStats());
    }
}
