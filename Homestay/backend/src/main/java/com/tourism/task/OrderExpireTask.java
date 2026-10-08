package com.tourism.task;

import com.tourism.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务:自动取消超时未支付订单并恢复库存(每 5 分钟执行一次)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpireTask {

    private final OrderService orderService;

    @Scheduled(fixedDelay = 5 * 60 * 1000, initialDelay = 60 * 1000)
    public void autoCancel() {
        try {
            int n = orderService.autoCancelExpired();
            if (n > 0) {
                log.info("定时取消超时未支付订单 {} 笔", n);
            }
        } catch (Exception e) {
            log.error("定时取消超时订单失败", e);
        }
    }
}
