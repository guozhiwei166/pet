package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourism.common.BizException;
import com.tourism.common.Constants;
import com.tourism.common.ResultCode;
import com.tourism.dto.RoomTypeSaveDTO;
import com.tourism.entity.Homestay;
import com.tourism.entity.Orders;
import com.tourism.entity.RoomInventory;
import com.tourism.entity.RoomType;
import com.tourism.mapper.HomestayMapper;
import com.tourism.mapper.OrdersMapper;
import com.tourism.mapper.RoomInventoryMapper;
import com.tourism.mapper.RoomTypeMapper;
import com.tourism.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 房型与库存服务实现
 */
@Service
@RequiredArgsConstructor
public class RoomTypeServiceImpl implements RoomTypeService {

    /** 自动生成未来库存的天数 */
    private static final int INVENTORY_DAYS = 60;

    private final RoomTypeMapper roomTypeMapper;
    private final HomestayMapper homestayMapper;
    private final OrdersMapper ordersMapper;
    private final RoomInventoryMapper roomInventoryMapper;

    @Override
    public List<RoomType> listOnSale(Long homestayId) {
        return roomTypeMapper.selectList(new LambdaQueryWrapper<RoomType>()
                .eq(RoomType::getHomestayId, homestayId)
                .eq(RoomType::getStatus, Constants.ROOM_ON)
                .orderByAsc(RoomType::getPrice)
                .orderByAsc(RoomType::getId));
    }

    @Override
    public List<RoomType> listAll(Long homestayId, Long merchantId) {
        requireHomestayOwner(homestayId, merchantId);
        return roomTypeMapper.selectList(new LambdaQueryWrapper<RoomType>()
                .eq(RoomType::getHomestayId, homestayId)
                .orderByAsc(RoomType::getId));
    }

    @Override
    public RoomType changeStatus(Long id, Long merchantId, String status) {
        RoomType rt = requireOwnedRoomType(id, merchantId);
        if (!Constants.ROOM_ON.equals(status) && !Constants.ROOM_OFF.equals(status)) {
            throw new BizException("房型状态不正确");
        }
        rt.setStatus(status);
        roomTypeMapper.updateById(rt);
        refreshHomestayMinPrice(rt.getHomestayId());
        return roomTypeMapper.selectById(rt.getId());
    }

    @Override
    @Transactional
    public RoomType create(RoomTypeSaveDTO dto, Long merchantId) {
        requireHomestayOwner(dto.getHomestayId(), merchantId);
        RoomType rt = new RoomType();
        BeanUtils.copyProperties(dto, rt, "id");
        rt.setStock(dto.getRoomCount());
        roomTypeMapper.insert(rt);

        // 自动生成未来日期库存
        generateInventory(rt, INVENTORY_DAYS);
        return roomTypeMapper.selectById(rt.getId());
    }

    @Override
    @Transactional
    public RoomType update(RoomTypeSaveDTO dto, Long merchantId) {
        RoomType rt = requireOwnedRoomType(dto.getId(), merchantId);
        // 只允许更新传入的非空字段,避免 DTO 缺省 null 覆盖已有值
        RoomType patch = new RoomType();
        patch.setId(rt.getId());
        if (StringUtils.hasText(dto.getName())) patch.setName(dto.getName());
        if (StringUtils.hasText(dto.getCoverImage())) patch.setCoverImage(dto.getCoverImage());
        if (StringUtils.hasText(dto.getBedType())) patch.setBedType(dto.getBedType());
        if (dto.getMaxGuests() != null) patch.setMaxGuests(dto.getMaxGuests());
        if (dto.getArea() != null) patch.setArea(dto.getArea());
        if (dto.getHasBreakfast() != null) patch.setHasBreakfast(dto.getHasBreakfast());
        if (dto.getPrice() != null) patch.setPrice(dto.getPrice());
        if (dto.getIntro() != null) patch.setIntro(dto.getIntro());
        if (dto.getFacilities() != null) patch.setFacilities(dto.getFacilities());
        if (StringUtils.hasText(dto.getStatus())) patch.setStatus(dto.getStatus());
        // 房间数量变化时,日期库存也需同步
        Integer oldCount = rt.getRoomCount() == null ? 0 : rt.getRoomCount();
        if (dto.getRoomCount() != null && !Objects.equals(oldCount, dto.getRoomCount())) {
            syncInventoryAfterRoomCountChange(rt, oldCount, dto.getRoomCount());
            patch.setRoomCount(dto.getRoomCount());
            patch.setStock(dto.getRoomCount());
        }
        roomTypeMapper.updateById(patch);
        refreshHomestayMinPrice(rt.getHomestayId());
        return roomTypeMapper.selectById(rt.getId());
    }

    private void syncInventoryAfterRoomCountChange(RoomType rt, int oldCount, int newCount) {
        int diff = newCount - oldCount;
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(INVENTORY_DAYS);
        if (diff > 0) {
            // 扩房:所有未来日期的可售数同步增加差额(含已被预订的日期,避免扩房后仍显示旧数量)
            List<RoomInventory> list = roomInventoryMapper.selectRange(rt.getId(), start, end);
            for (RoomInventory inv : list) {
                inv.setAvailableCount(inv.getAvailableCount() + diff);
                roomInventoryMapper.updateById(inv);
            }
        } else if (diff < 0) {
            // 缩房:未来日期库存取 [原库存, 新房间数] 较小者,防止超售
            List<RoomInventory> list = roomInventoryMapper.selectRange(rt.getId(), start, end);
            for (RoomInventory inv : list) {
                int capped = Math.min(inv.getAvailableCount(), newCount);
                if (capped != inv.getAvailableCount()) {
                    inv.setAvailableCount(capped);
                    roomInventoryMapper.updateById(inv);
                }
            }
        }
    }

    @Override
    @Transactional
    public void delete(Long id, Long merchantId) {
        RoomType rt = requireOwnedRoomType(id, merchantId);
        Long orderCount = ordersMapper.selectCount(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getRoomTypeId, id)
                .in(Orders::getStatus, Constants.ORDER_PENDING, Constants.ORDER_PAID,
                        Constants.ORDER_ARRIVED, Constants.ORDER_CHECKED_IN, Constants.ORDER_COMPLETED));
        if (orderCount > 0) {
            throw new BizException("该房型已有订单记录,无法删除,可改为下架");
        }
        roomTypeMapper.deleteById(id);
        roomInventoryMapper.delete(new LambdaQueryWrapper<RoomInventory>()
                .eq(RoomInventory::getRoomTypeId, id));
        refreshHomestayMinPrice(rt.getHomestayId());
    }

    /** 校验民宿归属当前商家 */
    private void requireHomestayOwner(Long homestayId, Long merchantId) {
        Homestay h = homestayMapper.selectById(homestayId);
        if (h == null || !Objects.equals(h.getMerchantId(), merchantId)) {
            throw new BizException(ResultCode.FORBIDDEN, "无权操作该民宿");
        }
    }

    /** 校验房型存在且归属当前商家 */
    private RoomType requireOwnedRoomType(Long id, Long merchantId) {
        RoomType rt = roomTypeMapper.selectById(id);
        if (rt == null) {
            throw new BizException(ResultCode.NOT_FOUND, "房型不存在");
        }
        Homestay h = homestayMapper.selectById(rt.getHomestayId());
        if (h == null || !Objects.equals(h.getMerchantId(), merchantId)) {
            throw new BizException(ResultCode.FORBIDDEN, "无权操作该房型");
        }
        return rt;
    }

    @Override
    public void generateInventory(RoomType rt, int days) {
        LocalDate today = LocalDate.now();
        // 仅补足未来缺失日期,已存在的(如被预订过)不重置,避免覆盖
        List<RoomInventory> existing = roomInventoryMapper.selectRange(rt.getId(), today, today.plusDays(days));
        Map<LocalDate, RoomInventory> map = new HashMap<>();
        for (RoomInventory inv : existing) {
            map.put(inv.getStayDate(), inv);
        }
        int count = rt.getRoomCount() == null ? 1 : rt.getRoomCount();
        for (int i = 0; i < days; i++) {
            LocalDate date = today.plusDays(i);
            if (!map.containsKey(date)) {
                insertInventory(rt.getId(), date, count);
            }
        }
    }

    private void insertInventory(Long roomTypeId, LocalDate date, int count) {
        RoomInventory inv = new RoomInventory();
        inv.setRoomTypeId(roomTypeId);
        inv.setStayDate(date);
        inv.setAvailableCount(count);
        roomInventoryMapper.insert(inv);
    }

    @Override
    public Map<LocalDate, Integer> calendarMap(Long roomTypeId, LocalDate start, LocalDate end) {
        Map<LocalDate, Integer> result = new HashMap<>();
        List<RoomInventory> list = roomInventoryMapper.selectRange(roomTypeId, start, end);
        for (RoomInventory inv : list) {
            result.put(inv.getStayDate(), inv.getAvailableCount());
        }
        return result;
    }

    private void refreshHomestayMinPrice(Long homestayId) {
        List<RoomType> ons = listOnSale(homestayId);
        Homestay h = homestayMapper.selectById(homestayId);
        if (h != null) {
            java.math.BigDecimal min = ons.stream()
                    .map(RoomType::getPrice)
                    .min(java.math.BigDecimal::compareTo)
                    .orElse(java.math.BigDecimal.ZERO);
            h.setMinPrice(min);
            homestayMapper.updateById(h);
        }
    }
}
