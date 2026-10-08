package com.tourism.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 商家经营统计 VO
 */
@Data
public class MerchantStatsVO {

    // ===== 概览卡片 =====
    private Long todayOrders;        // 今日订单
    private Long monthOrders;        // 本月订单
    private BigDecimal monthIncome;  // 本月已支付收入
    private BigDecimal occupancyRate;// 入住率 %
    private BigDecimal avgRating;    // 平均评分
    private Long totalRooms;         // 在售房型数

    // ===== 图表 =====
    private List<DateValue> last7DayOrders;   // 近7天订单趋势
    private List<MonthValue> monthIncomeTrend;// 近6月收入趋势
    private List<RankItem> roomTypeSales;     // 房型销量排行
    private Map<String, Long> orderStatusDist;// 订单状态分布

    @Data
    public static class DateValue {
        @JsonFormat(pattern = "MM-dd")
        private LocalDate date;
        private Long value;
    }

    @Data
    public static class MonthValue {
        private String month; // yyyy-MM
        private BigDecimal income;
        private Long orders;
    }

    @Data
    public static class RankItem {
        private String name;
        private Long sales;
        private BigDecimal income;
    }
}
