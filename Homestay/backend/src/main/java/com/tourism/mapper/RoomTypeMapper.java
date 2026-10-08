package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.entity.RoomType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RoomTypeMapper extends BaseMapper<RoomType> {

    /** 下单扣减冗余库存(原子,防止超扣) */
    @Update("UPDATE room_type SET stock = stock - #{n}, update_time = NOW() " +
            "WHERE id = #{id} AND deleted = 0 AND stock >= #{n}")
    int decrementStock(@Param("id") Long id, @Param("n") int n);

    /** 取消订单回补冗余库存(上限不超过房间总数) */
    @Update("UPDATE room_type SET stock = LEAST(stock + #{n}, room_count), update_time = NOW() " +
            "WHERE id = #{id} AND deleted = 0")
    int incrementStock(@Param("id") Long id, @Param("n") int n);
}
