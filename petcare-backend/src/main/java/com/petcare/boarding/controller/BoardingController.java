package com.petcare.boarding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.boarding.dto.OrderCreateDTO;
import com.petcare.boarding.entity.BoardingOrder;
import com.petcare.boarding.service.BoardingService;
import com.petcare.boarding.vo.OrderDetailVO;
import com.petcare.common.OperationLog;
import com.petcare.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BoardingController {

    private final BoardingService boardingService;

    @PostMapping("/boarding/availability")
    public Result<List<Map<String, Object>>> availability(@RequestBody Map<String, LocalDate> body) {
        return Result.ok(boardingService.availability(body.get("startDate"), body.get("endDate")));
    }

    @PostMapping("/boarding/orders")
    @OperationLog(module = "订单", action = "创建订单")
    public Result<BoardingOrder> create(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.ok(boardingService.create(dto));
    }

    @GetMapping("/orders")
    public Result<Page<BoardingOrder>> page(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) String status) {
        return Result.ok(boardingService.page(page, size, status));
    }

    @GetMapping("/orders/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        return Result.ok(boardingService.detail(id));
    }

    @PostMapping("/orders/{id}/pay")
    @OperationLog(module = "订单", action = "模拟支付")
    public Result<Void> pay(@PathVariable Long id) {
        boardingService.pay(id);
        return Result.ok();
    }

    @PostMapping("/orders/{id}/cancel")
    @OperationLog(module = "订单", action = "取消订单")
    public Result<Void> cancel(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        boardingService.cancel(id, body == null ? null : body.get("reason"));
        return Result.ok();
    }

    /** 可分配房间候选（手动分房弹窗用）：含房态、未来预约、冲突提示与可分配性 */
    @GetMapping("/orders/{id}/room-candidates")
    public Result<Map<String, Object>> roomCandidates(@PathVariable Long id) {
        return Result.ok(boardingService.roomCandidates(id));
    }

    /**
     * 分配房间（员工/管理员手动选房）：body { roomId, force }
     * force=false 时遇到软冲突（清洁中 / 与未来预约时段重叠）会返回提示，前端二次确认后带 force=true 重试。
     */
    @PostMapping("/orders/{id}/assign-room")
    @OperationLog(module = "订单", action = "分配房间")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public Result<Void> assignRoom(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long roomId = com.petcare.common.Params.requireLong(body, "roomId", "房间");
        boolean force = Boolean.TRUE.equals(body.get("force")) || "true".equals(String.valueOf(body.get("force")));
        boardingService.assignRoom(id, roomId, force);
        return Result.ok();
    }

    @PostMapping("/orders/{id}/check-in")
    @OperationLog(module = "订单", action = "办理入住")
    public Result<Void> checkIn(@PathVariable Long id) {
        boardingService.checkIn(id);
        return Result.ok();
    }

    @PostMapping("/orders/{id}/start-boarding")
    public Result<Void> startBoarding(@PathVariable Long id) {
        boardingService.startBoarding(id);
        return Result.ok();
    }

    @PostMapping("/orders/{id}/check-out")
    @OperationLog(module = "订单", action = "办理离店")
    public Result<Void> checkOut(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        boardingService.checkOut(id, com.petcare.common.Params.decimalOrNull(body, "weightOut", "离店体重"));
        return Result.ok();
    }

    @PostMapping("/orders/{id}/refund")
    public Result<Void> refundRequest(@PathVariable Long id) {
        boardingService.refundRequest(id);
        return Result.ok();
    }

    @PostMapping("/orders/{id}/refund-approve")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public Result<Void> refundApprove(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boardingService.refundApprove(id, Boolean.TRUE.equals(body.get("approve")));
        return Result.ok();
    }

    /** 升级房型（补差价），body: { roomTypeId: 目标房型 } */
    @PostMapping("/orders/{id}/upgrade-room")
    @OperationLog(module = "订单", action = "升级房型")
    public Result<Map<String, Object>> upgradeRoom(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        return Result.ok(boardingService.upgradeRoom(id, body.get("roomTypeId")));
    }
}
