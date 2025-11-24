package com.paymentsbe.product.api.controller;

import com.paymentsbe.product.api.dto.ProductSummaryResponse;
import com.paymentsbe.product.service.ProductQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductQueryService productQueryService;

    @GetMapping
    public List<ProductSummaryResponse> getProducts() {
        return productQueryService.getActiveProducts().stream()
                .map(ProductSummaryResponse::from)
                .toList();
    }
}