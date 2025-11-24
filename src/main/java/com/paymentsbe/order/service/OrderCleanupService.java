package com.paymentsbe.order.service;

import com.paymentsbe.order.domain.OrderStatus;
import com.paymentsbe.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCleanupService {

    private final OrderRepository orderRepository;

    /**
     * 오래된 PENDING 주문을 FAILED 로 마킹
     *
     * @param timeoutMinutes 생성 후 몇 분이 지나면 고아 주문으로 볼 것인지
     * @return FAILED 로 변경된 주문 개수
     */
    @Transactional
    public int markExpiredPendingOrdersAsFailed(long timeoutMinutes) {
        Instant threshold = Instant.now().minus(timeoutMinutes, ChronoUnit.MINUTES);

        int updated = orderRepository.updatePendingToFailedBefore(
                OrderStatus.PENDING,
                OrderStatus.FAILED,
                threshold
        );

        if (updated > 0) {
            log.info("[OrderCleanup] {} pending orders marked as FAILED (older than {} minutes)", updated, timeoutMinutes);
        } else {
            log.debug("[OrderCleanup] no pending orders to fail (timeoutMinutes={})", timeoutMinutes);
        }

        return updated;
    }
}