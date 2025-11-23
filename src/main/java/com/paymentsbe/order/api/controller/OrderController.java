package com.paymentsbe.order.api.controller;

import com.paymentsbe.order.api.dto.OrderCreateRequest;
import com.paymentsbe.order.api.dto.OrderCreateResponse;
import com.paymentsbe.order.api.dto.OrderDetailResponse;
import com.paymentsbe.order.api.dto.OrderSummaryResponse;
import com.paymentsbe.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderCreateResponse> create(
            @RequestBody OrderCreateRequest request
    ) {
        OrderCreateResponse response = orderService.createOrder(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public List<OrderSummaryResponse> getMyOrders(
            @RequestParam("userId") Long userId
    ) {
        return orderService.getOrdersForUser(userId);
    }

    @GetMapping("/{orderId}")
    public OrderDetailResponse getOrderDetail(
            @PathVariable("orderId") String orderId,
            @RequestParam("userId") Long userId
    ) {
        System.out.println("[OrderController] orderId=" + orderId + ", userId=" + userId);
        return orderService.getOrderDetail(orderId, userId);
    }
}
