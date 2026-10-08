package com.tourism.controller;

import com.tourism.common.Result;
import com.tourism.dto.HotelRoomCreateDTO;
import com.tourism.entity.HotelRoom;
import com.tourism.security.LoginUser;
import com.tourism.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourism.common.BizException;
import com.tourism.common.ResultCode;
import com.tourism.entity.Homestay;
import com.tourism.mapper.HomestayMapper;
import com.tourism.mapper.HotelRoomMapper;

import java.util.List;
import java.util.Objects;

/**
 * 物理房间管理(商家为某民宿/房型录入房间号,便于演示"房间管理";库存以日期库存为准)
 */
@RestController
@RequestMapping("/api/hotel-room")
@RequiredArgsConstructor
public class HotelRoomController {

    private final HotelRoomMapper hotelRoomMapper;
    private final HomestayMapper homestayMapper;
    private final MerchantService merchantService;

    /** 某民宿的房间列表(商家) */
    @GetMapping("/list")
    public Result<List<HotelRoom>> list(Long homestayId) {
        LoginUser.requireRole("MERCHANT");
        merchantService.requireApproved();
        requireOwner(homestayId);
        return Result.success(hotelRoomMapper.selectList(new LambdaQueryWrapper<HotelRoom>()
                .eq(HotelRoom::getHomestayId, homestayId)
                .orderByAsc(HotelRoom::getRoomNumber)));
    }

    /** 新增房间(批量房间号) */
    @PostMapping("/create")
    public Result<List<HotelRoom>> create(@Valid @RequestBody HotelRoomCreateDTO dto) {
        LoginUser.requireRole("MERCHANT");
        merchantService.requireApproved();
        requireOwner(dto.getHomestayId());
        for (String no : dto.getRoomNumbers()) {
            HotelRoom r = new HotelRoom();
            r.setHomestayId(dto.getHomestayId());
            r.setRoomTypeId(dto.getRoomTypeId());
            r.setRoomNumber(no.trim());
            r.setFloor(dto.getFloor());
            r.setRemark(dto.getRemark());
            hotelRoomMapper.insert(r);
        }
        return Result.success(hotelRoomMapper.selectList(new LambdaQueryWrapper<HotelRoom>()
                .eq(HotelRoom::getHomestayId, dto.getHomestayId())
                .orderByAsc(HotelRoom::getRoomNumber)));
    }

    /** 删除房间 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        LoginUser.requireRole("MERCHANT");
        merchantService.requireApproved();
        HotelRoom r = hotelRoomMapper.selectById(id);
        if (r == null) {
            return Result.success();
        }
        requireOwner(r.getHomestayId());
        hotelRoomMapper.deleteById(id);
        return Result.success();
    }

    private void requireOwner(Long homestayId) {
        Homestay h = homestayMapper.selectById(homestayId);
        if (h == null || !Objects.equals(h.getMerchantId(), merchantService.currentMerchant().getId())) {
            throw new BizException(ResultCode.FORBIDDEN, "无权操作该民宿");
        }
    }
}
