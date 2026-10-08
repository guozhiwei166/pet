package com.petcare.care.controller;

import com.petcare.care.service.CareService;
import com.petcare.care.service.DailyReportService;
import com.petcare.common.Result;
import com.petcare.boarding.mapper.BoardingOrderMapper;
import com.petcare.pet.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 照护接口（员工端，SecurityConfig 已限定 STAFF/ADMIN）
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CareController {

    private final CareService careService;
    private final DailyReportService dailyReportService;
    private final BoardingOrderMapper orderMapper;
    private final PetService petService;

    @GetMapping("/care/workbench")
    public Result<Map<String, Object>> workbench(@RequestParam(required = false) String date) {
        return Result.ok(careService.workbench(date == null ? null : LocalDate.parse(date)));
    }

    @GetMapping("/care/tasks")
    public Result<List<Map<String, Object>>> tasks(@RequestParam(required = false) String date,
                                                   @RequestParam(required = false) String type,
                                                   @RequestParam(required = false) String status) {
        return Result.ok(careService.tasks(date == null ? null : LocalDate.parse(date), type, status));
    }

    @PostMapping("/care/tasks/{id}/complete")
    public Result<Void> complete(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        careService.complete(id, body);
        return Result.ok();
    }

    @PostMapping("/care/tasks/{id}/skip")
    public Result<Void> skip(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        careService.skip(id, body == null ? null : body.get("remark"));
        return Result.ok();
    }

    /** 订单照护时间线（员工/管理员查看；主人端走日报） */
    @GetMapping("/care/orders/{orderId}/timeline")
    public Result<List<Map<String, Object>>> timeline(@PathVariable Long orderId) {
        return Result.ok(careService.orderTimeline(orderId));
    }

    /** 每日日报：订单维度（主人看自己的，员工/管理员看全部） */
    @GetMapping("/orders/{orderId}/daily-reports")
    public Result<List<com.petcare.care.entity.PetDailyReport>> dailyReports(@PathVariable Long orderId) {
        var order = orderMapper.selectById(orderId);
        if (order == null) throw new com.petcare.common.BusinessException("订单不存在");
        var lu = com.petcare.security.SecurityUtils.current();
        if ("ROLE_USER".equals(lu.getRole()) && !order.getUserId().equals(lu.getId())) {
            throw new com.petcare.common.BusinessException(403, "无权查看该订单");
        }
        return Result.ok(dailyReportService.listByOrder(orderId));
    }

    /** 宠物维度日报（成长档案）：属主本人 / STAFF / ADMIN */
    @GetMapping("/pets/{petId}/daily-reports")
    public Result<List<com.petcare.care.entity.PetDailyReport>> petReports(
            @PathVariable Long petId, @RequestParam(defaultValue = "90") int days) {
        petService.assertVisible(petId);
        return Result.ok(dailyReportService.listByPet(petId, days));
    }

    /** 员工为日报补充备注与照片（仅工作人员：路径不在 /api/care/** 下，需显式限权） */
    @PostMapping("/orders/{orderId}/daily-reports/{date}/note")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public Result<Void> note(@PathVariable Long orderId, @PathVariable String date,
                             @RequestBody Map<String, String> body) {
        dailyReportService.saveNote(orderId, LocalDate.parse(date), body.get("note"), body.get("photos"),
                com.petcare.security.SecurityUtils.currentId());
        return Result.ok();
    }
}
