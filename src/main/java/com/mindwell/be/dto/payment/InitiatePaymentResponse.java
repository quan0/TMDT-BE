package com.mindwell.be.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "InitiatePaymentResponse")
public record InitiatePaymentResponse(
        Integer paymentId,
        @Schema(allowableValues = {"pending", "paid", "failed", "cancelled"})
        String status,
        @Schema(description = "Next URL for the client (provider redirect for external methods)")
        String redirectUrl,
        String message
) {
}
