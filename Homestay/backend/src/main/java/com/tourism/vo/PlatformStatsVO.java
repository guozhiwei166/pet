package com.tourism.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 平台统计 VO(管理员首页)
 */
@Data
public class PlatformStatsVO {

    // ===== 概览 =====
    private Long userCount;          // 用户数
    private Long merchantCount;      // 商家数
    private Long homestayCount;      // 民宿数
    private Long orderCount;         // 订单总数
    private Long todayOrderCount;    // 今日订单
    private BigDecimal monthIncome;  // 本月交易额
    private Long pendingHomestay;    // 待审核民宿
    private Long pendingMerchant;    // 待审核商家
    private Long pendingReport;      // 待处理举报

    // ===== 图表 =====
    private List<UserGrowth> userGrowth;          // 近7日新增用户
    private List<OrderTrend> orderTrend;          // 近7日订单
    private List<MonthIncome> monthIncomeTrend;   // 近6月交易额
    private List<RankItem> hotDestinations;       // 热门目的地(按民宿数/收藏)
    private List<RankItem> hotHomestays;          // 热门民宿
    private Map<String, Long> orderStatusDist;    // 订单状态分布

    @Data
    public static class UserGrowth {
        @JsonFormat(pattern = "MM-dd")
        private LocalDate date;
        private Long count;
    }

    @Data
    public static class OrderTrend {
        @JsonFormat(pattern = "MM-dd")
        private LocalDate date;
        private Long count;
    }

    @Data
    public static class MonthIncome {
        private String month;
        private BigDecimal income;
    }

    @Data
    public static class RankItem {
        private String name;
        private Long value;
    }
}
