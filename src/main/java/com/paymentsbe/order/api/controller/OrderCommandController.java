package com.paymentsbe.order.api.controller;

import com.paymentsbe.order.api.dto.CreateOrderRequest;
import com.paymentsbe.order.api.dto.CreateOrderResponse;
import com.paymentsbe.order.service.OrderCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order-commands")
@RequiredArgsConstructor
public class OrderCommandController {

    private final OrderCommandService orderCommandService;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestBody CreateOrderRequest request
    ) {
        CreateOrderResponse response = orderCommandService.createOrder(request);
        return ResponseEntity.ok(response);
    }
}
