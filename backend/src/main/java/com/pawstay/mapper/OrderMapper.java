package com.pawstay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pawstay.entity.BoardingOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderMapper extends BaseMapper<BoardingOrder> {

    /** 指定房型在 [start, end) 区间的有效占用数（用于库存与智能排笼校验） */
    @Select("SELECT COUNT(*) FROM pet_order WHERE room_id = #{roomId} " +
            "AND status NOT IN ('CANCELLED','REJECTED') AND start_date < #{end} AND end_date > #{start}")
    int countOverlap(@Param("roomId") Long roomId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    /** 近 N 日订单量与营收（管理端数据大屏） */
    @Select("SELECT DATE(create_time) AS d, COUNT(*) AS cnt, IFNULL(SUM(total_amount), 0) AS amount " +
            "FROM pet_order WHERE create_time >= #{start} AND status NOT IN ('CANCELLED','REJECTED') " +
            "GROUP BY DATE(create_time) ORDER BY d")
    List<Map<String, Object>> selectDailyStats(@Param("start") LocalDateTime start);

    /** 接单量 Top 商家 */
    @Select("SELECT m.shop_name AS name, COUNT(*) AS cnt FROM pet_order o " +
            "JOIN pet_merchant m ON o.merchant_id = m.id " +
            "WHERE o.status NOT IN ('CANCELLED','REJECTED') " +
            "GROUP BY o.merchant_id, m.shop_name ORDER BY cnt DESC LIMIT 5")
    List<Map<String, Object>> selectTopMerchants();

    /** 累计营收（已支付且未取消/拒单） */
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM pet_order WHERE pay_time IS NOT NULL AND status NOT IN ('CANCELLED','REJECTED')")
    BigDecimal sumRevenue();
}
