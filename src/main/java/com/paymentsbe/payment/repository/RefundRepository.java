package com.paymentsbe.payment.repository;


import com.paymentsbe.payment.domain.Payment;
import com.paymentsbe.payment.domain.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByPayment(Payment payment);
}
