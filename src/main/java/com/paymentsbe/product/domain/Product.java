package com.paymentsbe.product.domain;

import com.paymentsbe.common.entity.TimeBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "product",
        indexes = {
                @Index(name = "idx_product_sku", columnList = "sku", unique = true)
        }
)
@Getter
@SuperBuilder
@NoArgsConstructor
public class Product extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 외부 시스템과 연동에 쓰일 상품 식별자
    @Column(name = "sku", nullable = false, unique = true, length = 64)
    private String sku;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    // 판매가 (리스트 가격)
    @Column(name = "list_price_krw", nullable = false)
    private Long listPriceKrw;

    // 판매중 여부
    @Getter
    @Column(name = "active", nullable = false)
    private boolean active;

    // --- 도메인 메서드 ---

    public void changeInfo(String name, String description, Long listPriceKrw) {
        this.name = name;
        this.description = description;
        this.listPriceKrw = listPriceKrw;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
