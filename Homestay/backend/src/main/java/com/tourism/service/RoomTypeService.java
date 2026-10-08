package com.tourism.service;

import com.tourism.dto.RoomTypeSaveDTO;
import com.tourism.entity.RoomType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 房型与库存服务
 */
public interface RoomTypeService {

    /** 某民宿的上架房型列表(游客详情页) */
    List<RoomType> listOnSale(Long homestayId);

    /** 某民宿全部房型(商家管理,校验归属防越权) */
    List<RoomType> listAll(Long homestayId, Long merchantId);

    /** 商家上架/下架房型 */
    RoomType changeStatus(Long id, Long merchantId, String status);

    /** 商家新增房型(自动生成未来 60 天日期库存) */
    RoomType create(RoomTypeSaveDTO dto, Long merchantId);

    /** 商家编辑房型 */
    RoomType update(RoomTypeSaveDTO dto, Long merchantId);

    /** 商家删除房型(存在订单禁止) */
    void delete(Long id, Long merchantId);

    /** 生成某房型未来 days 天的日期库存(幂等:已存在的不覆盖) */
    void generateInventory(RoomType roomType, int days);

    /** 查询某房型日期区间的库存,Map<date, availableCount> */
    Map<LocalDate, Integer> calendarMap(Long roomTypeId, LocalDate start, LocalDate end);
}
