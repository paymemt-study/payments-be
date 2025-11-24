package com.paymentsbe.order.repository;

import com.paymentsbe.order.domain.Order;
import com.paymentsbe.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByExternalId(String externalId);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Order> findByExternalIdAndUserId(String externalId, Long userId);


    /**
     * 오래된 PENDING 주문을 FAILED 로 일괄 변경
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Order o
           set o.status = :failed
         where o.status = :pending
           and o.createdAt < :threshold
        """)
    int updatePendingToFailedBefore(
            @Param("pending") OrderStatus pending,
            @Param("failed") OrderStatus failed,
            @Param("threshold") Instant threshold
    );
}
