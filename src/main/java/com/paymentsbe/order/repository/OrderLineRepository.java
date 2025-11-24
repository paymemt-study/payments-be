package com.paymentsbe.order.repository;

import com.paymentsbe.order.domain.Order;
import com.paymentsbe.order.domain.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

    List<OrderLine> findByOrder(Order order);
}
