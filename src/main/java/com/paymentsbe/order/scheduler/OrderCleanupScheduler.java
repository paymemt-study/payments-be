package com.paymentsbe.order.scheduler;

import com.paymentsbe.order.service.OrderCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCleanupScheduler {

    private final OrderCleanupService orderCleanupService;

    /**
     * PENDING → FAILED 기준 시간(분)
     * 예: 30분 동안 PENDING이면 고아 주문으로 보고 FAILED 처리
     */
    @Value("${order.cleanup.pending-timeout-minutes:30}")
    private long pendingTimeoutMinutes;

    /**
     * 10분마다 한 번씩 고아 주문 정리
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 */10 * * * *")
    public void cleanPendingOrders() {
        int updated = orderCleanupService.markExpiredPendingOrdersAsFailed(pendingTimeoutMinutes);
        if (updated > 0) {
            log.info("[OrderCleanupScheduler] cleaned {} stale pending orders", updated);
        }
    }
}