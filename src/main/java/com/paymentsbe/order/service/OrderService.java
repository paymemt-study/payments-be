package com.paymentsbe.order.service;

import com.paymentsbe.common.exception.BusinessException;
import com.paymentsbe.order.api.dto.OrderCreateRequest;
import com.paymentsbe.order.api.dto.OrderCreateResponse;
import com.paymentsbe.order.api.dto.OrderDetailResponse;
import com.paymentsbe.order.api.dto.OrderSummaryResponse;
import com.paymentsbe.order.domain.Order;
import com.paymentsbe.order.repository.OrderRepository;
import com.paymentsbe.payment.domain.Payment;
import com.paymentsbe.payment.repository.PaymentRepository;
import com.paymentsbe.user.domain.User;
import com.paymentsbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.paymentsbe.common.exception.ErrorCode;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository; // 비회원 허용할거면 Optional
    private final PaymentRepository paymentRepository;

    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request) {

        User user = null;
        if (request.userId() != null) {
            user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        }

        Order order = Order.create(user, request.amount());
        Order saved = orderRepository.save(order);

        return new OrderCreateResponse(
                saved.getExternalId(),
                saved.getTotalAmountKrw()
        );
    }

    public List<OrderSummaryResponse> getOrdersForUser(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream()
                .map(OrderSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(String externalOrderId, Long userId) {
        Order order = orderRepository
                .findByExternalIdAndUserId(externalOrderId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        Payment payment = paymentRepository
                .findTopByOrderOrderByIdDesc(order)
                .orElse(null); // 결제 없으면 null

        return OrderDetailResponse.from(order, payment);
    }
}
