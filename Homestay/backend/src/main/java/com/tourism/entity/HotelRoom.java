package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物理房间(供商家房间管理录入;库存扣减以日期库存表为准)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hotel_room")
public class HotelRoom extends BaseEntity {

    private Long homestayId;
    private Long roomTypeId;
    private String roomNumber;
    private String floor;
    private String remark;
}
