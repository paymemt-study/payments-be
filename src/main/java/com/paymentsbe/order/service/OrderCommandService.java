package com.paymentsbe.order.service;

import com.paymentsbe.common.exception.BusinessException;
import com.paymentsbe.common.exception.ErrorCode;
import com.paymentsbe.order.api.dto.CreateOrderRequest;
import com.paymentsbe.order.api.dto.CreateOrderResponse;
import com.paymentsbe.order.domain.Order;
import com.paymentsbe.order.domain.OrderLine;
import com.paymentsbe.order.repository.OrderRepository;
import com.paymentsbe.product.domain.Product;
import com.paymentsbe.product.repository.ProductRepository;
import com.paymentsbe.user.domain.User;
import com.paymentsbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {

        // 1) 유저 조회 (로그인 붙으면 SecurityContext에서 꺼내는 걸로 변경)
        User user = null;
        if (request.userId() != null) {
            user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        }

        // 2) 비어있는 주문 생성
        Order order = Order.createEmpty(user);

        // 3) 각 상품별 OrderLine 생성
        if (request.items() == null || request.items().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE); // 최소 1개는 있어야 함
        }

        request.items().forEach(item -> {
            if (item.productId() == null || item.quantity() == null || item.quantity() <= 0) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }

            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

            // OrderLine.create 내부에서 order.addOrderLine(line) 호출 + 금액 재계산
            OrderLine.create(order, product, item.quantity());
        });

        // 4) 방어적 체크: totalAmountKrw > 0
        if (order.getTotalAmountKrw() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 5) 저장 (OrderLine은 cascade로 함께 저장)
        Order saved = orderRepository.save(order);

        // 6) 프론트/결제 위젯에 넘길 정보 반환
        return new CreateOrderResponse(
                saved.getExternalId(),    // orderId (Toss widget의 orderId와 매핑)
                saved.getTotalAmountKrw(),
                saved.getCurrency()
        );
    }
}