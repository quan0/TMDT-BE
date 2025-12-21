package com.mindwell.be.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(name = "SubscriptionConfirmationDto")
public record SubscriptionConfirmationDto(
        SubscriptionPlanDto plan,
        @Schema(description = "User subscription status", example = "active")
        String subscriptionStatus,
        @Schema(description = "Subscription expiry date", example = "2026-01-21")
        LocalDate expiryDate,
        @Schema(description = "Payment id used for this subscription", example = "123")
        Integer paymentId,
        @Schema(description = "Payment status", allowableValues = {"pending", "paid", "failed", "cancelled"}, example = "paid")
        String paymentStatus
) {
}
