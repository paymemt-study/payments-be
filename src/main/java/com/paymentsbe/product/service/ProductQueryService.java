package com.paymentsbe.product.service;

import com.paymentsbe.product.domain.Product;
import com.paymentsbe.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductRepository productRepository;

    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrueOrderByIdAsc();
    }
}
