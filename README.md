# **Payments BE – 결제 시스템 데모 서버**

Spring Boot 기반 결제/주문 시스템 데모 프로젝트입니다.

TossPayments를 이용한 카드 결제 · 취소 · Webhook 동기화를 포함합니다.
## 📦 패키지 구조

```text
com.paymentsbe
├─ common
│  ├─ config              # 공통 설정 (JPA Auditing, Security, Swagger, TossProperties 등)
│  ├─ entity              # 공통 BaseEntity (TimeBaseEntity)
│  ├─ exception           # 공통 예외/에러 응답 (BusinessException, ErrorCode, ApiError, GlobalExceptionHandler)
│  └─ util                # 공통 유틸 (JsonUtils 등)
│
├─ order
│  ├─ api
│  │  ├─ controller       # 주문 조회/생성 API (OrderController, OrderCommandController)
│  │  └─ dto              # 주문 요청/응답 DTO
│  ├─ domain              # Order, OrderLine, OrderStatus 도메인 엔티티
│  ├─ repository          # OrderRepository, OrderLineRepository
│  ├─ scheduler           # 주문 정리 스케줄러 (OrderCleanupScheduler)
│  └─ service             # 주문 조회/생성/정리 서비스 (OrderService, OrderCommandService, OrderCleanupService)
│
├─ payment
│  ├─ api
│  │  ├─ controller       # 결제 승인/취소 API, Toss Webhook 수신 컨트롤러
│  │  └─ dto              # 결제 요청/응답, Webhook Payload DTO
│  ├─ domain              # Payment, Refund, PaymentStatus, PaymentProvider, PaymentWebhookEvent
│  ├─ repository          # PaymentRepository, RefundRepository, PaymentWebhookEventRepository
│  └─ service             # 결제 승인/실패/취소/웹훅 처리 서비스, TossClient (외부 연동)
│
├─ product
│  ├─ api
│  │  ├─ controller       # 상품 조회 API (ProductController)
│  │  └─ dto              # ProductSummaryResponse
│  ├─ domain              # Product 엔티티 (SKU, 이름, 가격, active 등)
│  ├─ repository          # ProductRepository
│  └─ service             # 상품 조회 서비스 (ProductQueryService)
│
└─ user
   ├─ domain              # User 엔티티
   └─ repository          # UserRepository
   ```

##  🧩 구성 요소 요약

### 1. Product (상품)
- 상품 정보 저장
- 판매가(listPriceKrw), 이름(name), 설명(description)
- REST API → /api/products
   
### 2. Order + OrderLine
- Order는 “주문 헤더”
- OrderLine은 “주문 상품 상세”
- **주문 생성 시 OrderLine 합산으로 totalAmountKrw 자동 계산**

연관관계:
```
Order (1) --- (N) OrderLine --- (1) Product
```

### **3. 결제(Payment)**

- 각 주문(Order)에 대해 1개의 Payment 생성
- providerPaymentKey(Toss paymentKey) 저장
- 상태:
    - PENDING → PAID → PARTIAL_REFUND → REFUNDED
    - 실패 시 FAILED

### **4. TossPayments Webhook**

- 엔드포인트: /api/webhooks/toss
- 이벤트 타입: PAYMENT_STATUS_CHANGED

멱등성 처리:
```java
if (eventRepository.existsByEventId(eventId)) {
    return; // 중복 Webhook 무시
}
```
status 분기 처리 (payload.data.status):
```
DONE → 승인 성공
CANCELED / PARTIAL_CANCELED → 환불 처리
FAILED → 결제 실패
```
모든 웹훅 이벤트는 payment_webhook_event 테이블에 저장됨.