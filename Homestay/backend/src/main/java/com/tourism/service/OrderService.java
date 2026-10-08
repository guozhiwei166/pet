package com.tourism.service;

import com.tourism.common.PageVO;
import com.tourism.dto.OrderCreateDTO;
import com.tourism.entity.Orders;
import com.tourism.vo.OrderVO;

import java.time.LocalDate;

/**
 * 订单服务(预订全生命周期 + 防超卖)
 */
public interface OrderService {

    /** 试算价格(下单前确认页展示,不产生订单) */
    OrderVO preview(com.tourism.dto.OrderPreviewDTO dto);

    /** 创建订单(校验日期/库存/房费,事务内行锁防超卖) */
    OrderVO create(OrderCreateDTO dto, Long userId);

    /** 模拟支付:幂等,仅待支付可支付成功 */
    OrderVO pay(Long orderId, Long userId, String payType);

    /** 用户取消(待支付或已支付待入住;恢复库存) */
    OrderVO cancelByUser(Long orderId, Long userId);

    /** 用户已到达民宿(已支付->已到店,仅入住日当天起;商家方可接待) */
    OrderVO arriveByUser(Long orderId, Long userId);

    /** 用户立即退房(入住中 -> 已完成;入住后不再退款) */
    OrderVO earlyCheckoutByUser(Long orderId, Long userId);

    /** 商家取消/拒单(已支付-待入住可取消,恢复库存) */
    OrderVO cancelByMerchant(Long orderId, Long merchantId, String remark);

    /** 商家办理入住(已支付 -> 入住中) */
    OrderVO checkIn(Long orderId, Long merchantId);

    /** 商家办理退房/离店(入住中 -> 已完成) */
    OrderVO checkOut(Long orderId, Long merchantId);

    /** 用户订单列表(分页) */
    PageVO<OrderVO> pageByUser(Long userId, int current, int size, String status);

    /** 商家订单列表(分页,按民宿) */
    PageVO<OrderVO> pageByMerchant(Long merchantId, int current, int size, String status, Long homestayId);

    /** 订单详情(用户/商家/管理员按角色可见) */
    OrderVO detail(Long orderId, Long userId, String role, Long merchantId);

    /** 内部:按 id 查询实体 */
    Orders getById(Long id);

    /** 管理员全平台订单分页(带用户头像/昵称) */
    PageVO<OrderVO> pageForAdmin(int current, int size, String status, String orderNo, String keyword);

    /** 定时任务:自动取消超时未支付订单 */
    int autoCancelExpired();

    /** 判定用户某民宿是否可评价(至少一笔已完成订单) */
    boolean canReview(Long userId, Long homestayId);
}
