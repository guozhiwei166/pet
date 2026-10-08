package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 房型日期库存 —— 防超卖核心
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("room_inventory")
public class RoomInventory extends BaseEntity {

    private Long roomTypeId;
    private LocalDate stayDate;
    private Integer availableCount;
}
