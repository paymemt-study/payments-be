package com.paymentsbe.order.domain;

import com.paymentsbe.common.entity.TimeBaseEntity;
import com.paymentsbe.product.domain.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "order_line")
@Getter
@SuperBuilder
@NoArgsConstructor
public class OrderLine extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: 주문
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // FK: 상품
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 상품 1개 단가(당시 가격 스냅샷)
    @Column(name = "unit_price_krw", nullable = false)
    private Long unitPriceKrw;

    // 수량
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // 라인 총 금액 = unit_price * quantity
    @Column(name = "line_amount_krw", nullable = false)
    private Long lineAmountKrw;

    // -------------------------------
    // 정적 팩토리 + 도메인 메서드
    // -------------------------------

    /**
     * Product의 현재 listPriceKrw를 기준으로 OrderLine 생성
     */
    public static OrderLine create(Order order, Product product, int quantity) {
        Long unitPrice = product.getListPriceKrw();
        Long lineAmount = unitPrice * quantity;

        OrderLine line = OrderLine.builder()
                .product(product)
                .unitPriceKrw(unitPrice)
                .quantity(quantity)
                .lineAmountKrw(lineAmount)
                .build();

        // 연관관계 설정 및 합계 계산은 Order 쪽에서 통제
        order.addOrderLine(line);
        return line;
    }

    /**
     * Order가 연관관계 설정할 때만 호출하는 내부용 메서드
     * (패키지 프라이빗)
     */
    void assignOrder(Order order) {
        this.order = order;
    }

    /**
     * 수량 변경 시 라인 금액 재계산 + 주문 합계 재계산
     */
    public void changeQuantity(int newQuantity) {
        this.quantity = newQuantity;
        this.lineAmountKrw = this.unitPriceKrw * newQuantity;

        if (this.order != null) {
            this.order.recalculateTotalAmount();
        }
    }
}