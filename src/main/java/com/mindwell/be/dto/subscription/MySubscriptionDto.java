package com.mindwell.be.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(name = "MySubscription")
public record MySubscriptionDto(
        SubscriptionPlanDto plan,
        @Schema(allowableValues = {"active", "expired", "cancelled"})
        String status,
        LocalDate expiryDate,
        Integer paymentId
) {
}
