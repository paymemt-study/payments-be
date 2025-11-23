package com.paymentsbe.payment.repository;

import com.paymentsbe.order.domain.Order;
import com.paymentsbe.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByProviderPaymentKey(String providerPaymentKey);

    Optional<Payment> findTopByOrderOrderByIdDesc(Order order);
}
