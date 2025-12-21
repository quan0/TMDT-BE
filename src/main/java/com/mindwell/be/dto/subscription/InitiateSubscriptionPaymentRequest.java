package com.mindwell.be.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "InitiateSubscriptionPaymentRequest")
public record InitiateSubscriptionPaymentRequest(
        @NotBlank
        @Schema(description = "Payment method key", example = "vnpay")
        String methodKey
) {
}
