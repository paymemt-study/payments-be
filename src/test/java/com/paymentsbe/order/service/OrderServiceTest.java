package com.paymentsbe.order.service;

import com.paymentsbe.common.exception.BusinessException;
import com.paymentsbe.common.exception.ErrorCode;
import com.paymentsbe.order.api.dto.OrderCreateRequest;
import com.paymentsbe.order.api.dto.OrderCreateResponse;
import com.paymentsbe.order.api.dto.OrderDetailResponse;
import com.paymentsbe.order.api.dto.OrderSummaryResponse;
import com.paymentsbe.order.domain.Order;
import com.paymentsbe.order.repository.OrderRepository;
import com.paymentsbe.payment.domain.Payment;
import com.paymentsbe.payment.domain.PaymentProvider;
import com.paymentsbe.payment.domain.PaymentStatus;
import com.paymentsbe.payment.repository.PaymentRepository;
import com.paymentsbe.user.domain.User;
import com.paymentsbe.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    // -----------------------------------------------------
    // createOrder()
    // -----------------------------------------------------
    @Test
    @DisplayName("createOrder - userId가 있을 때 정상 생성")
    void createOrder_success() {

        OrderCreateRequest req = new OrderCreateRequest(1L, 50000L);

        User mockUser = new User(1L, "test@test.com", "테스트유저");
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        Order mockOrder = Order.create(mockUser, 50000L);
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        OrderCreateResponse res = orderService.createOrder(req);

        assertThat(res.orderId()).isNotNull();
        assertThat(res.amount()).isEqualTo(50000);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("createOrder - userId가 존재하지 않을 때 예외")
    void createOrder_userNotFound() {

        OrderCreateRequest req = new OrderCreateRequest(999L, 50000L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    // -----------------------------------------------------
    // getOrdersForUser()
    // -----------------------------------------------------
    @Test
    @DisplayName("getOrdersForUser - 정상 조회")
    void getOrdersForUser_success() {

        User user = new User(1L, "test@test.com", "테스트");
        Order order1 = Order.create(user, 50000L);
        Order order2 = Order.create(user, 60000L);

        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(order1, order2));

        List<OrderSummaryResponse> list = orderService.getOrdersForUser(1L);

        assertThat(list).hasSize(2);
        assertThat(list.get(0).amount()).isEqualTo(50000);
        assertThat(list.get(1).amount()).isEqualTo(60000);
    }

    // -----------------------------------------------------
    // getOrderDetail()
    // -----------------------------------------------------
    @Test
    @DisplayName("getOrderDetail - 결제 포함 정상 조회")
    void getOrderDetail_success() {

        User user = new User(1L, "test@test.com", "테스트");
        Order mockOrder = Order.create(user, 50000L);
        String externalId = mockOrder.getExternalId();

        when(orderRepository.findByExternalIdAndUserId(externalId, 1L))
                .thenReturn(Optional.of(mockOrder));

        OffsetDateTime now = OffsetDateTime.now();

        Payment payment = Payment.builder()
                .id(10L)
                .order(mockOrder)
                .provider(PaymentProvider.TOSS)
                .method("CARD")
                .amountKrw(50000L)
                .status(PaymentStatus.PAID)
                .approvedAt(now)
                .build();

        when(paymentRepository.findTopByOrderOrderByIdDesc(mockOrder))
                .thenReturn(Optional.of(payment));

        OrderDetailResponse res = orderService.getOrderDetail(externalId, 1L);

        assertThat(res.orderId()).isEqualTo(externalId);
        assertThat(res.payment()).isNotNull();
        assertThat(res.payment().paymentId()).isEqualTo(10L);
        assertThat(res.payment().provider()).isEqualTo("TOSS");
        assertThat(res.payment().method()).isEqualTo("CARD");
        assertThat(res.payment().amountKrw()).isEqualTo(50000L);
        assertThat(res.payment().status()).isEqualTo(PaymentStatus.PAID);
        assertThat(res.payment().approvedAt()).isEqualTo(now.toInstant());
    }

    @Test
    @DisplayName("getOrderDetail - 주문이 존재하지 않을 때 예외 발생")
    void getOrderDetail_orderNotFound() {

        when(orderRepository.findByExternalIdAndUserId("ORD-xxx", 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                orderService.getOrderDetail("ORD-xxx", 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("getOrderDetail - 결제 정보가 없어도 정상 응답")
    void getOrderDetail_noPayment() {

        User user = new User(1L, "test@test.com", "테스트");
        Order order = Order.create(user, 50000L);

        when(orderRepository.findByExternalIdAndUserId(any(), any()))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findTopByOrderOrderByIdDesc(order))
                .thenReturn(Optional.empty());

        OrderDetailResponse res = orderService.getOrderDetail(order.getExternalId(), 1L);

        assertThat(res.payment()).isNull();
        assertThat(res.orderId()).isEqualTo(order.getExternalId());
    }
}