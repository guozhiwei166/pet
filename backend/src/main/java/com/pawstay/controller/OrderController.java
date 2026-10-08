package com.pawstay.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pawstay.common.Result;
import com.pawstay.dto.OrderCreateDTO;
import com.pawstay.entity.BoardingOrder;
import com.pawstay.service.OrderService;
import com.pawstay.vo.OrderDetailVO;
import com.pawstay.vo.OrderVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

/** 寄养订单全流程：下单/支付/取消/接单/拒单/入住/离店 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Result<BoardingOrder> create(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.ok(orderService.create(dto));
    }

    @GetMapping
    public Result<Page<OrderVO>> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) String status) {
        return Result.ok(orderService.page(page, size, status));
    }

    @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        return Result.ok(orderService.detail(id));
    }

    @PostMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id) {
        orderService.pay(id);
        return Result.ok();
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return Result.ok();
    }

    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        orderService.confirm(id);
        return Result.ok();
    }

    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        orderService.reject(id, body == null ? null : body.get("reason"));
        return Result.ok();
    }

    @PostMapping("/{id}/checkin")
    public Result<Void> checkin(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        orderService.checkin(id, toDecimal(body == null ? null : body.get("weightIn")));
        return Result.ok();
    }

    @PostMapping("/{id}/checkout")
    public Result<Void> checkout(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        orderService.checkout(id, toDecimal(body == null ? null : body.get("weightOut")));
        return Result.ok();
    }

    private BigDecimal toDecimal(Object v) {
        return v == null ? null : new BigDecimal(String.valueOf(v));
    }
}
