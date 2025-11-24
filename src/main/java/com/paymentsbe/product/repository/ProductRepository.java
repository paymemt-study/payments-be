package com.paymentsbe.product.repository;

import com.paymentsbe.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    // 활성 상품만 조회(데모용)
    List<Product> findByActiveTrueOrderByIdAsc();
}
