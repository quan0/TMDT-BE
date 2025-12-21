package com.mindwell.be.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(name = "MockPaymentSuccessDto")
public record MockPaymentSuccessDto(
        @Schema(example = "123")
        Integer paymentId,

        @Schema(example = "subscription")
        String paymentType,

        @Schema(example = "paid")
        String status,

        @Schema(description = "Related entity id (appointmentId or subscriptionId)", example = "3")
        Integer relatedId,

        @Schema(description = "Created user subscription id (subscription payments only)", example = "10")
        Integer userSubId,

        @Schema(description = "Subscription expiry date (subscription payments only)", example = "2026-01-21")
        LocalDate expiryDate
) {
}
