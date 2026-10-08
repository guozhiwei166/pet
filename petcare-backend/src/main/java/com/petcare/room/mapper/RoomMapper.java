package com.petcare.room.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.room.entity.Room;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

public interface RoomMapper extends BaseMapper<Room> {

    /**
     * 全店房号（含逻辑删除的记录）。
     * uk_room_no 唯一索引不区分 deleted，被逻辑删除的房间仍占着房号，
     * 因此新建/批量建房的房号查重必须带上已删除记录，否则会撞唯一索引报 500。
     */
    @Select("SELECT room_no FROM room")
    List<String> selectAllRoomNos();

    /**
     * 原子分配房间（手动分房）：
     * - 房间未维修、且没有别的"已入住/寄养中"订单占用时才可以分配（在住冲突恒定拒绝）；
     * - allowOverlap=0（未二次确认）时，还要求该房间在目标日期区间内没有别的已确认预约，
     *   由数据库条件判定，避免两笔并发分配把同一间房重复预订给重叠时段；
     * - allowOverlap=1（管理员二次确认后）只挡在住冲突，允许覆盖未来预约。
     * 房间"已预约/入住中"状态由订单时段派生，不改 room.status。
     * 返回值 = 受影响行数（1 成功，0 失败）
     */
    @Update("""
            UPDATE room r
            SET r.current_order_id = #{orderId}, r.update_time = NOW()
            WHERE r.id = #{roomId}
              AND r.deleted = 0
              AND r.status <> 'MAINTENANCE'
              AND NOT EXISTS (
                  SELECT 1 FROM boarding_order o
                  WHERE o.room_id = r.id
                    AND o.id <> #{orderId}
                    AND o.deleted = 0
                    AND (
                        o.status IN ('CHECKED_IN', 'BOARDING')
                        OR (#{allowOverlap} = 0 AND o.status = 'CONFIRMED'
                            AND o.start_date < #{endDate} AND o.end_date > #{startDate})
                    )
              )
            """)
    int tryAssign(@Param("roomId") Long roomId,
                  @Param("orderId") Long orderId,
                  @Param("startDate") LocalDate startDate,
                  @Param("endDate") LocalDate endDate,
                  @Param("allowOverlap") int allowOverlap);
}
