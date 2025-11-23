package com.paymentsbe.order.domain;

import com.paymentsbe.common.entity.TimeBaseEntity;
import com.paymentsbe.user.domain.User;
import com.paymentsbe.common.exception.BusinessException;
import com.paymentsbe.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

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

    private static String generateExternalId() {
        String suffix = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 8);
        return "ORD-" + suffix;
    }


    // 비즈니스 메서드
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

    /**
     * 결제 승인 성공 시 호출
     */
    public void markPaid() {
        if (!isPending()) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }
        this.status = OrderStatus.PAID;
    }

    /**
     * 결제 실패 시 호출 (PG 오류, 승인 실패 등)
     * 이미 PAID/CANCELED 상태라면 굳이 예외 던질 필요 없이 무시해도 됨.
     */
    public void markFailed() {
        if (isPending()) {
            this.status = OrderStatus.FAILED;
        }
    }

    /**
     * 주문 취소(환불 완료 후) 시 호출
     */
    public void cancel() {
        if (!isPaid()) {
            throw new BusinessException(ErrorCode.CANNOT_CANCEL_ORDER);
        }
        this.status = OrderStatus.CANCELED;
    }
}