package com.paymentsbe.order.domain;

import com.paymentsbe.common.entity.TimeBaseEntity;
import com.paymentsbe.common.exception.BusinessException;
import com.paymentsbe.common.exception.ErrorCode;
import com.paymentsbe.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "`order`")
@Getter
@SuperBuilder
@NoArgsConstructor
public class Order extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 외부 노출용 주문번호 (ORD-xxxx)
    @Column(name = "external_id", nullable = false, unique = true, length = 64)
    private String externalId;

    // 회원/비회원 모두 고려해서 nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "total_amount_krw", nullable = false)
    private Long totalAmountKrw;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    // --- OrderLine 컬렉션 연관관계 추가 ---
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @org.hibernate.annotations.BatchSize(size = 50)
    @lombok.Builder.Default
    private List<OrderLine> orderLines = new ArrayList<>();

    // TODO: 쿠폰 붙이면 coupon 필드 추가 (Coupon 엔티티와 연관관계)

    public static Order create(User user, Long amountKrw) {
        return Order.builder()
                .externalId(generateExternalId())
                .user(user)               // 비회원이면 null 전달 가능
                .totalAmountKrw(amountKrw)
                .currency("KRW")
                .status(OrderStatus.PENDING)
                .build();
    }

    public static Order createEmpty(User user) {
        return Order.builder()
                .externalId(generateExternalId())
                .user(user)
                .totalAmountKrw(0L)
                .currency("KRW")
                .status(OrderStatus.PENDING)
                .build();
    }

    private static String generateExternalId() {
        String suffix = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 8);
        return "ORD-" + suffix;
    }

    // ==============================
    //    연관관계 편의 메서드
    // ==============================

    public void addOrderLine(OrderLine line) {
        if (line == null) {
            return;
        }
        this.orderLines.add(line);
        line.assignOrder(this);
        recalculateTotalAmount();
    }

    public void removeOrderLine(OrderLine line) {
        if (line == null) {
            return;
        }
        this.orderLines.remove(line);
        line.assignOrder(null);
        recalculateTotalAmount();
    }

    public void recalculateTotalAmount() {
        long sum = this.orderLines.stream()
                .mapToLong(OrderLine::getLineAmountKrw)
                .sum();
        this.totalAmountKrw = sum;
    }

    // ==============================
    //      비즈니스 메서드
    // ==============================

    public boolean isPending() {
        return this.status == OrderStatus.PENDING;
    }

    public boolean isPaid() {
        return this.status == OrderStatus.PAID;
    }

    public boolean isCanceled() {
        return this.status == OrderStatus.CANCELED;
    }

    public boolean isFailed() {
        return this.status == OrderStatus.FAILED;
    }

    public void markPaid() {
        if (!isPending()) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }
        this.status = OrderStatus.PAID;
    }

    public void markFailed() {
        if (isPending()) {
            this.status = OrderStatus.FAILED;
        }
    }

    public void cancel() {
        if (!isPaid()) {
            throw new BusinessException(ErrorCode.CANNOT_CANCEL_ORDER);
        }
        this.status = OrderStatus.CANCELED;
    }
}