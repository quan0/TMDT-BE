package com.mindwell.be.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "InitiateAppointmentPaymentResponse")
public record InitiateAppointmentPaymentResponse(
        Integer paymentId,
        String status,
        @Schema(description = "Redirect URL for external payment providers (null for MindPoints)")
        String redirectUrl,
        @Schema(description = "Optional message for UI")
        String message
) {
}
