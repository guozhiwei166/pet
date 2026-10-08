package com.petcare.room.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.room.entity.RoomType;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface RoomTypeMapper extends BaseMapper<RoomType> {

    /**
     * 行级写锁：锁定目标房型行，用于下单等"先查库存再插入"场景的并发串行化。
     * 同房型并发下单会在此等待前一个事务提交后再重查库存，避免超卖窗口。
     */
    @Select("SELECT * FROM room_type WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    RoomType selectByIdForUpdate(@Param("id") Long id);
}
