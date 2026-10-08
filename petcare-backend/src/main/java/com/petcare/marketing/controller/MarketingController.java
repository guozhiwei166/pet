package com.petcare.marketing.controller;

import com.petcare.common.Result;
import com.petcare.marketing.entity.Announcement;
import com.petcare.marketing.entity.Coupon;
import com.petcare.marketing.service.MarketingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MarketingController {

    private final MarketingService marketingService;

    // ---------- 优惠券 ----------
    @GetMapping("/coupons/available")
    public Result<List<Coupon>> available() {
        return Result.ok(marketingService.availableCoupons());
    }

    @GetMapping("/coupons/mine")
    public Result<List<com.petcare.marketing.vo.MyCouponVO>> mine() {
        return Result.ok(marketingService.myCoupons());
    }

    @PostMapping("/coupons/{id}/claim")
    public Result<Void> claim(@PathVariable Long id) {
        marketingService.claim(id);
        return Result.ok();
    }

    /** 为订单应用最优券（金额由服务端按订单计算，不接受客户端传入） */
    @PostMapping("/orders/{id}/apply-coupon")
    public Result<BigDecimal> applyCoupon(@PathVariable Long id) {
        return Result.ok(marketingService.applyCouponToOrder(id));
    }

    // 管理端券模板
    @GetMapping("/admin/coupons")
    @PreAuthorize("hasRole('ADMIN')")
    @com.petcare.security.RequirePermission("marketing:coupon")
    public Result<List<Coupon>> allCoupons() {
        return Result.ok(marketingService.allCoupons());
    }

    @PostMapping("/admin/coupons")
    @PreAuthorize("hasRole('ADMIN')")
    @com.petcare.security.RequirePermission("marketing:coupon")
    public Result<Void> saveCoupon(@RequestBody Coupon coupon) {
        marketingService.saveCoupon(coupon);
        return Result.ok();
    }

    @DeleteMapping("/admin/coupons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @com.petcare.security.RequirePermission("marketing:coupon")
    public Result<Void> deleteCoupon(@PathVariable Long id) {
        marketingService.deleteCoupon(id);
        return Result.ok();
    }

    // ---------- 公告 ----------
    @GetMapping("/announcements")
    public Result<List<Announcement>> announcements(@RequestParam(defaultValue = "false") boolean all) {
        return Result.ok(marketingService.listAnnouncements(!all));
    }

    @PostMapping("/admin/announcements")
    @PreAuthorize("hasRole('ADMIN')")
    @com.petcare.security.RequirePermission("marketing:announcement")
    public Result<Void> saveAnnouncement(@RequestBody Announcement a) {
        marketingService.saveAnnouncement(a);
        return Result.ok();
    }

    @DeleteMapping("/admin/announcements/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @com.petcare.security.RequirePermission("marketing:announcement")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        marketingService.deleteAnnouncement(id);
        return Result.ok();
    }
}
