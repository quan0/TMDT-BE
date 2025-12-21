package com.mindwell.be.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "PaymentListItemDto")
public record PaymentListItemDto(
        Integer paymentId,
        @Schema(allowableValues = {"subscription", "mindpoints", "appointment"})
        String paymentType,
        @Schema(allowableValues = {"pending", "paid", "failed", "cancelled"})
        String status,
        BigDecimal amount,
        Integer relatedId,
        String methodKey,
        String methodDisplayName
) {
}
