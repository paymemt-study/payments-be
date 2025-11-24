package com.paymentsbe.product.api.dto;

public record ProductSummaryResponse(
        Long id,
        String name,
        Long listPriceKrw
) {
    public static ProductSummaryResponse from(com.paymentsbe.product.domain.Product p) {
        return new ProductSummaryResponse(
                p.getId(),
                p.getName(),
                p.getListPriceKrw()
        );
    }
}