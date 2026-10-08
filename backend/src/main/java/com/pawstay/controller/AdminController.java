package com.pawstay.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pawstay.common.Result;
import com.pawstay.dto.AuditDTO;
import com.pawstay.entity.Merchant;
import com.pawstay.entity.User;
import com.pawstay.service.AdminService;
import com.pawstay.service.MerchantService;
import com.pawstay.service.OrderService;
import com.pawstay.util.UserContext;
import com.pawstay.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 平台管理端：数据大屏、商家审核、用户与订单监管 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private OrderService orderService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        UserContext.requireRole("ADMIN");
        return Result.ok(adminService.stats());
    }

    @GetMapping("/merchants")
    public Result<IPage<Merchant>> merchants(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) String status) {
        UserContext.requireRole("ADMIN");
        return Result.ok(merchantService.pageForAdmin(page, size, status));
    }

    @PostMapping("/merchants/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestBody AuditDTO dto) {
        UserContext.requireRole("ADMIN");
        merchantService.audit(id, dto);
        return Result.ok();
    }

    @GetMapping("/users")
    public Result<Page<User>> users(@RequestParam(defaultValue = "1") long page,
                                    @RequestParam(defaultValue = "10") long size,
                                    @RequestParam(required = false) String keyword) {
        UserContext.requireRole("ADMIN");
        return Result.ok(adminService.pageUsers(page, size, keyword));
    }

    @GetMapping("/orders")
    public Result<Page<OrderVO>> orders(@RequestParam(defaultValue = "1") long page,
                                        @RequestParam(defaultValue = "10") long size,
                                        @RequestParam(required = false) String status) {
        UserContext.requireRole("ADMIN");
        return Result.ok(orderService.page(page, size, status));
    }
}
