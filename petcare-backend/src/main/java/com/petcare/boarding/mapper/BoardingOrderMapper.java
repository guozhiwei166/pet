package com.petcare.boarding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.boarding.entity.BoardingOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface BoardingOrderMapper extends BaseMapper<BoardingOrder> {

    /** 按日聚合营业额/订单量（统计口径：payTime 落在区间且非取消/退款；deleted=0 手动过滤逻辑删除） */
    @Select("""
            SELECT DATE_FORMAT(pay_time, '%m-%d') AS day,
                   COUNT(*)                          AS orders,
                   COALESCE(SUM(total_amount), 0)    AS revenue
            FROM boarding_order
            WHERE deleted = 0
              AND pay_time >= #{from}
              AND status NOT IN ('CANCELLED', 'REFUNDING', 'REFUNDED')
            GROUP BY DATE_FORMAT(pay_time, '%m-%d')
            """)
    List<Map<String, Object>> sumDailyRevenue(@Param("from") LocalDateTime from);

    /** 今日营收（已支付、非取消/退款） */
    @Select("""
            SELECT COALESCE(SUM(total_amount), 0)
            FROM boarding_order
            WHERE deleted = 0
              AND pay_time >= #{start} AND pay_time < #{end}
              AND status NOT IN ('CANCELLED', 'REFUNDING', 'REFUNDED')
            """)
    BigDecimal sumRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 今日入住/离店数 */
    @Select("""
            SELECT COUNT(*) FROM boarding_order
            WHERE deleted = 0 AND checkin_time >= #{start} AND checkin_time < #{end}
            """)
    long countCheckinBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Select("""
            SELECT COUNT(*) FROM boarding_order
            WHERE deleted = 0 AND checkout_time >= #{start} AND checkout_time < #{end}
            """)
    long countCheckoutBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
