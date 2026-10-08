package com.petcare.boarding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.boarding.entity.BoardingOrderServiceItem;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface BoardingOrderServiceItemMapper extends BaseMapper<BoardingOrderServiceItem> {

    /** 热门服务 TopN：按服务名聚合销量，由数据库排序避免全表加载（表无逻辑删除列） */
    @Select("""
            SELECT service_name AS name, COUNT(*) AS value
            FROM boarding_order_service
            GROUP BY service_name
            ORDER BY value DESC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> hotServices(@org.apache.ibatis.annotations.Param("limit") int limit);
}
