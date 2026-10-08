package com.tourism.controller;

import com.tourism.common.PageVO;
import com.tourism.common.Result;
import com.tourism.dto.OrderCreateDTO;
import com.tourism.dto.OrderPreviewDTO;
import com.tourism.security.LoginUser;
import com.tourism.service.MerchantService;
import com.tourism.service.OrderService;
import com.tourism.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预订订单接口:用户端(下单/支付/取消/查询) + 商家端(履约/取消)
 */
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MerchantService merchantService;

    // ==================== 用户端 ====================

    /** 价格试算(不产生订单) */
    @PostMapping("/preview")
    public Result<OrderVO> preview(@Valid @RequestBody OrderPreviewDTO dto) {
        return Result.success(orderService.preview(dto));
    }

    /** 创建订单 */
    @PostMapping("/create")
    public Result<OrderVO> create(@Valid @RequestBody OrderCreateDTO dto) {
        LoginUser.requireRole("USER");
        return Result.success(orderService.create(dto, LoginUser.userId()));
    }

    /** 模拟支付(幂等:待支付才可支付成功) */
    @PostMapping("/{id}/pay")
    public Result<OrderVO> pay(@PathVariable Long id, @RequestParam(required = false) String payType) {
        LoginUser.requireRole("USER");
        return Result.success(orderService.pay(id, LoginUser.userId(), payType));
    }

    /** 用户取消(待支付/已支付待入住可取消,已支付取消退款) */
    @PutMapping("/{id}/cancel")
    public Result<OrderVO> cancel(@PathVariable Long id) {
        LoginUser.requireRole("USER");
        return Result.success(orderService.cancelByUser(id, LoginUser.userId()));
    }

    /** 用户已到达民宿(待入住->已到店,商家方可接待;限入住日当天起) */
    @PutMapping("/{id}/arrive")
    public Result<OrderVO> arrive(@PathVariable Long id) {
        LoginUser.requireRole("USER");
        return Result.success(orderService.arriveByUser(id, LoginUser.userId()));
    }

    /** 用户立即退房(入住中,不退款) */
    @PutMapping("/{id}/early-checkout")
    public Result<OrderVO> earlyCheckout(@PathVariable Long id) {
        LoginUser.requireRole("USER");
        return Result.success(orderService.earlyCheckoutByUser(id, LoginUser.userId()));
    }

    /** 我的订单 */
    @GetMapping("/my")
    public Result<PageVO<OrderVO>> my(@RequestParam(defaultValue = "1") int current,
                                      @RequestParam(defaultValue = "6") int size,
                                      @RequestParam(required = false) String status) {
        LoginUser.requireRole("USER");
        return Result.success(orderService.pageByUser(LoginUser.userId(), current, size, status));
    }

    // ==================== 商家端 ====================

    /** 商家订单列表 */
    @GetMapping("/merchant/list")
    public Result<PageVO<OrderVO>> merchantList(@RequestParam(defaultValue = "1") int current,
                                                @RequestParam(defaultValue = "6") int size,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) Long homestayId) {
        LoginUser.requireRole("MERCHANT");
        merchantService.requireApproved();
        return Result.success(orderService.pageByMerchant(merchantService.currentMerchant().getId(),
                current, size, status, homestayId));
    }

    /** 商家办理入住 */
    @PutMapping("/merchant/{id}/check-in")
    public Result<OrderVO> checkIn(@PathVariable Long id) {
        LoginUser.requireRole("MERCHANT");
        merchantService.requireApproved();
        return Result.success(orderService.checkIn(id, merchantService.currentMerchant().getId()));
    }

    /** 商家办理退房/完成 */
    @PutMapping("/merchant/{id}/check-out")
    public Result<OrderVO> checkOut(@PathVariable Long id) {
        LoginUser.requireRole("MERCHANT");
        merchantService.requireApproved();
        return Result.success(orderService.checkOut(id, merchantService.currentMerchant().getId()));
    }

    /** 商家取消订单(已支付待入住可取消,恢复库存) */
    @PutMapping("/merchant/{id}/cancel")
    public Result<OrderVO> merchantCancel(@PathVariable Long id, @RequestParam(required = false) String remark) {
        LoginUser.requireRole("MERCHANT");
        merchantService.requireApproved();
        return Result.success(orderService.cancelByMerchant(id, merchantService.currentMerchant().getId(), remark));
    }

    /** 订单详情(用户本人/商家/管理员按权限) */
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        LoginUser.require();
        String role = LoginUser.role();
        Long merchantId = null;
        if ("MERCHANT".equals(role)) {
            merchantId = merchantService.currentMerchant().getId();
        }
        return Result.success(orderService.detail(id, LoginUser.userId(), role, merchantId));
    }
}
