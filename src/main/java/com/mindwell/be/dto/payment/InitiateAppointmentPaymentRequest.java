package com.mindwell.be.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "InitiateAppointmentPaymentRequest")
public record InitiateAppointmentPaymentRequest(
        @NotBlank
        @Schema(description = "Payment method key", example = "vnpay")
        String methodKey,

        @Schema(description = "Meeting platform id selected on checkout (optional)", example = "1")
        Integer platformId,

        @Schema(description = "video|chat|voice selected on checkout (optional)", example = "video")
        String serviceType,

        @Schema(description = "Contact full name (optional; overrides appointment contact)", example = "Nguyễn Minh Anh")
        String contactFullName,

        @Schema(description = "Contact email (optional; overrides appointment contact)", example = "user@example.com")
        String contactEmail,

        @Schema(description = "Contact phone (optional; overrides appointment contact)", example = "0912345678")
        String contactPhone
) {
}
