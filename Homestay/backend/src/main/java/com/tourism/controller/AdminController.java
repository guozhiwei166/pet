package com.tourism.controller;

import com.tourism.common.PageVO;
import com.tourism.common.Result;
import com.tourism.entity.Merchant;
import com.tourism.entity.Orders;
import com.tourism.entity.Report;
import com.tourism.security.RequireRole;
import com.tourism.service.AdminService;
import com.tourism.service.OrderService;
import com.tourism.service.ReportService;
import com.tourism.service.ReviewService;
import com.tourism.vo.HomestayVO;
import com.tourism.vo.ReviewVO;
import com.tourism.vo.OrderVO;
import com.tourism.vo.UserAdminVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台管理接口(仅管理员)
 */
@RestController
@RequestMapping("/api/admin")
@RequireRole("ADMIN")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;
    private final ReportService reportService;
    private final ReviewService reviewService;

    // ===== 用户管理 =====

    @GetMapping("/user/page")
    public Result<PageVO<UserAdminVO>> users(@RequestParam(defaultValue = "1") int current,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String role) {
        return Result.success(adminService.pageUsers(current, size, keyword, role));
    }

    /** 重置用户密码为 123456 */
    @PutMapping("/user/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        adminService.resetPassword(id);
        return Result.success();
    }

    @PutMapping("/user/{id}/status")
    public Result<Void> changeUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminService.changeUserStatus(id, status);
        return Result.success();
    }

    // ===== 商家审核 =====

    @GetMapping("/merchant/page")
    public Result<PageVO<Merchant>> merchants(@RequestParam(defaultValue = "1") int current,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(required = false) String keyword) {
        return Result.success(adminService.pageMerchants(current, size, status, keyword));
    }

    @PutMapping("/merchant/{id}/audit")
    public Result<Void> auditMerchant(@PathVariable Long id,
                                      @RequestParam String status,
                                      @RequestParam(required = false) String remark) {
        adminService.auditMerchant(id, status, remark);
        return Result.success();
    }

    // ===== 民宿审核 =====

    @GetMapping("/homestay/page")
    public Result<PageVO<HomestayVO>> homestays(@RequestParam(defaultValue = "1") int current,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String auditStatus,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Long destinationId) {
        return Result.success(adminService.pageHomestaysForAdmin(current, size, auditStatus, keyword, destinationId));
    }

    @PutMapping("/homestay/{id}/audit")
    public Result<Void> auditHomestay(@PathVariable Long id,
                                      @RequestParam String status,
                                      @RequestParam(required = false) String remark) {
        adminService.auditHomestay(id, status, remark);
        return Result.success();
    }

    @PutMapping("/homestay/{id}/offline")
    public Result<Void> offlineHomestay(@PathVariable Long id) {
        adminService.offlineHomestay(id);
        return Result.success();
    }

    // ===== 订单监管 =====

    @GetMapping("/order/page")
    public Result<PageVO<OrderVO>> orders(@RequestParam(defaultValue = "1") int current,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false) String orderNo,
                                         @RequestParam(required = false) String keyword) {
        return Result.success(orderService.pageForAdmin(current, size, status, orderNo, keyword));
    }

    // ===== 评价管理 =====

    @GetMapping("/review/all")
    public Result<PageVO<ReviewVO>> allReviews(@RequestParam(defaultValue = "1") int current,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(required = false) Integer status) {
        return Result.success(reviewService.pageAllForAdmin(current, size, status));
    }

    @PutMapping("/review/{id}/hide")
    public Result<Void> hideReview(@PathVariable Long id) {
        reviewService.hideByAdmin(id);
        return Result.success();
    }

    @PutMapping("/review/{id}/restore")
    public Result<Void> restoreReview(@PathVariable Long id) {
        reviewService.restoreByAdmin(id);
        return Result.success();
    }

    // ===== 举报处理 =====

    @GetMapping("/report/page")
    public Result<PageVO<Report>> reports(@RequestParam(defaultValue = "1") int current,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(required = false) Integer reportType) {
        return Result.success(reportService.pageForAdmin(current, size, status, reportType));
    }

    @PutMapping("/report/{id}/handle")
    public Result<Void> handleReport(@PathVariable Long id,
                                     @RequestParam String status,
                                     @RequestParam(required = false) String remark) {
        reportService.handle(id, status, remark);
        return Result.success();
    }
}
