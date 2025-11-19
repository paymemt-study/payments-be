package com.paymentsbe.order.repository;

import com.paymentsbe.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByExternalId(String externalId);
}
