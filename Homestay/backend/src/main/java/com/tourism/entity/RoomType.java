package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 房型
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("room_type")
public class RoomType extends BaseEntity {

    private Long homestayId;
    private String name;
    private String coverImage;
    private String bedType;
    private Integer maxGuests;
    private BigDecimal area;
    /** 0=不含早 1=含早 */
    private Integer hasBreakfast;
    private BigDecimal price;
    /** 房间总数 */
    private Integer roomCount;
    /** 剩余可售总间数(冗余展示) */
    private Integer stock;
    private String intro;
    private String facilities;
    /** ON / OFF */
    private String status;
}
