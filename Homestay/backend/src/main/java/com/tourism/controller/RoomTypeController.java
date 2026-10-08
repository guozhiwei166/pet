package com.tourism.controller;

import com.tourism.common.Result;
import com.tourism.common.ResultCode;
import com.tourism.common.BizException;
import com.tourism.dto.RoomTypeSaveDTO;
import com.tourism.entity.RoomType;
import com.tourism.security.LoginUser;
import com.tourism.security.Public;
import com.tourism.service.MerchantService;
import com.tourism.service.RoomTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 房型接口:游客浏览(上架房型+库存日历),商家管理
 */
@RestController
@RequestMapping("/api/room-type")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService roomTypeService;
    private final MerchantService merchantService;

    /** 游客:民宿上架房型 */
    @Public
    @GetMapping("/on-sale")
    public Result<List<RoomType>> onSale(@RequestParam Long homestayId) {
        return Result.success(roomTypeService.listOnSale(homestayId));
    }

    /** 游客:某房型日期区间剩余库存(价格日历) start/end = yyyy-MM-dd */
    @Public
    @GetMapping("/calendar")
    public Result<Map<LocalDate, Integer>> calendar(@RequestParam Long roomTypeId,
                                                    @RequestParam String start,
                                                    @RequestParam String end) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate s = LocalDate.parse(start, f);
        LocalDate e = LocalDate.parse(end, f);
        if (e.isBefore(s)) {
            throw new BizException(ResultCode.BAD_REQUEST, "结束日期不能早于开始日期");
        }
        return Result.success(roomTypeService.calendarMap(roomTypeId, s, e));
    }

    // ==================== 商家管理(需已审核商家) ====================

    /** 民宿全部房型(商家,校验民宿归属) */
    @GetMapping("/merchant/list")
    public Result<List<RoomType>> listByHomestay(@RequestParam Long homestayId) {
        LoginUser.requireRole("MERCHANT");
        merchantService.requireApproved();
        return Result.success(roomTypeService.listAll(homestayId, merchantService.currentMerchant().getId()));
    }

    /** 房型上架/下架 */
    @PutMapping("/merchant/{id}/status")
    public Result<RoomType> changeStatus(@PathVariable Long id, @RequestParam String status) {
        LoginUser.requireRole("MERCHANT");
        merchantService.requireApproved();
        return Result.success(roomTypeService.changeStatus(id, merchantService.currentMerchant().getId(), status));
    }

    @PostMapping("/merchant")
    public Result<RoomType> create(@Valid @RequestBody RoomTypeSaveDTO dto) {
        LoginUser.requireRole("MERCHANT");
        merchantService.requireApproved();
        return Result.success(roomTypeService.create(dto, merchantService.currentMerchant().getId()));
    }

    @PutMapping("/merchant")
    public Result<RoomType> update(@Valid @RequestBody RoomTypeSaveDTO dto) {
        LoginUser.requireRole("MERCHANT");
        merchantService.requireApproved();
        return Result.success(roomTypeService.update(dto, merchantService.currentMerchant().getId()));
    }

    @DeleteMapping("/merchant/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        LoginUser.requireRole("MERCHANT");
        merchantService.requireApproved();
        roomTypeService.delete(id, merchantService.currentMerchant().getId());
        return Result.success();
    }
}
