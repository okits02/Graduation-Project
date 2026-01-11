package com.example.order_service.scheduler;

import com.example.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCleanupScheduler {
    private final OrderService orderService;
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupExpiredOrders() {
        orderService.cleanupExpiredPendingOrders();
    }
}
