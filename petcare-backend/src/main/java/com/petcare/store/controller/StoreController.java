package com.petcare.store.controller;

import com.petcare.common.Result;
import com.petcare.store.entity.ServiceBooking;
import com.petcare.store.entity.StoreProfile;
import com.petcare.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // 门店信息（公开读；管理端写）
    @GetMapping("/store/profile")
    public Result<StoreProfile> profile() {
        return Result.ok(storeService.profile());
    }

    @PutMapping("/store/profile")
    public Result<Void> saveProfile(@RequestBody StoreProfile profile) {
        storeService.saveProfile(profile);
        return Result.ok();
    }

    // 服务预约
    @GetMapping("/service-bookings/mine")
    public Result<List<ServiceBooking>> myBookings() {
        return Result.ok(storeService.myBookings());
    }

    /** 当日预约看板（员工端；全店数据，需限 STAFF/ADMIN） */
    @GetMapping("/service-bookings")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public Result<List<ServiceBooking>> bookingsByDate(@RequestParam(required = false) String date) {
        return Result.ok(storeService.bookingsByDate(date == null ? null : LocalDate.parse(date)));
    }

    @PostMapping("/service-bookings")
    public Result<ServiceBooking> create(@RequestBody Map<String, Object> body) {
        return Result.ok(storeService.create(body));
    }

    @PostMapping("/service-bookings/{id}/{action}")
    public Result<Void> transition(@PathVariable Long id, @PathVariable String action) {
        storeService.transition(id, action);
        return Result.ok();
    }
}
