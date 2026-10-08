package com.petcare.statistics;

import com.petcare.common.Result;
import com.petcare.log.entity.OperationLogEntity;
import com.petcare.log.mapper.OperationLogMapper;
import com.petcare.security.SecurityUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理端聚合接口（SecurityConfig 已限定 /api/admin/** 为 ADMIN）
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final OperationLogMapper logMapper;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.ok(adminService.dashboard());
    }

    @GetMapping("/stats/trend")
    public Result<List<Map<String, Object>>> trend(@RequestParam(defaultValue = "7") int days) {
        return Result.ok(adminService.trend(Math.min(Math.max(days, 7), 30)));
    }

    @GetMapping("/stats/services")
    public Result<List<Map<String, Object>>> hotServices() {
        return Result.ok(adminService.hotServices());
    }

    @GetMapping("/stats/staff")
    public Result<List<Map<String, Object>>> staffStats() {
        return Result.ok(adminService.staffStats());
    }

    @GetMapping("/users")
    public Result<List<Map<String, Object>>> users(@RequestParam(required = false) String keyword) {
        return Result.ok(adminService.users(keyword));
    }

    @PutMapping("/users/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.resetPassword(id, body.get("password"));
        return Result.ok();
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> toggleUser(@PathVariable Long id) {
        adminService.toggleUserStatus(id);
        return Result.ok();
    }

    @GetMapping("/logs")
    public Result<Page<OperationLogEntity>> logs(@RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "20") long size) {
        SecurityUtils.requireRole("ROLE_ADMIN");
        return Result.ok(logMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<OperationLogEntity>().orderByDesc(OperationLogEntity::getId)));
    }
}
