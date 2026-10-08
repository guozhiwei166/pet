package com.tourism.mapper;

import com.tourism.entity.RoomInventory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

/**
 * 房型日期库存 Mapper
 * 防超卖核心:selectForUpdate 行锁 + decrement 原子扣减(仅当剩余足够)
 */
@Mapper
public interface RoomInventoryMapper extends BaseMapper<RoomInventory> {

    /** 查询并锁定某房型某晚库存(需在事务中使用) */
    @Select("SELECT * FROM room_inventory WHERE room_type_id = #{roomTypeId} AND stay_date = #{date} AND deleted = 0 FOR UPDATE")
    RoomInventory selectForUpdate(@Param("roomTypeId") Long roomTypeId, @Param("date") LocalDate date);

    /** 原子扣减:仅当 available_count >= need 时成功,返回受影响行数 */
    @Update("UPDATE room_inventory SET available_count = available_count - #{need}, update_time = NOW() " +
            "WHERE room_type_id = #{roomTypeId} AND stay_date = #{date} AND deleted = 0 AND available_count >= #{need}")
    int decrement(@Param("roomTypeId") Long roomTypeId, @Param("date") LocalDate date, @Param("need") int need);

    /** 取消订单时回补库存 */
    @Update("UPDATE room_inventory SET available_count = available_count + #{need}, update_time = NOW() " +
            "WHERE room_type_id = #{roomTypeId} AND stay_date = #{date} AND deleted = 0")
    int increment(@Param("roomTypeId") Long roomTypeId, @Param("date") LocalDate date, @Param("need") int need);

    /** 查询某房型在某区间的库存记录,用于日历展示 */
    @Select("SELECT * FROM room_inventory WHERE room_type_id = #{roomTypeId} AND stay_date BETWEEN #{start} AND #{end} AND deleted = 0")
    List<RoomInventory> selectRange(@Param("roomTypeId") Long roomTypeId,
                                    @Param("start") LocalDate start,
                                    @Param("end") LocalDate end);
}
