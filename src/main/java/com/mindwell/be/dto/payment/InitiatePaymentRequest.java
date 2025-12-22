package com.mindwell.be.dto.payment;

import com.mindwell.be.entity.enums.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "InitiatePaymentRequest")
public record InitiatePaymentRequest(
        @NotNull
        @Schema(description = "What you are paying for", allowableValues = {"appointment", "subscription"}, example = "subscription")
        PaymentType paymentType,

        @NotNull
        @Schema(description = "Related entity id (appointmentId or subscriptionId)", example = "3")
        Integer relatedId,

        @NotBlank
        @Schema(description = "Payment method key", example = "vnpay")
        String methodKey,

        @Schema(description = "Meeting platform id selected on checkout (appointment only)", example = "1")
        Integer platformId,

        @Schema(description = "video|chat|voice selected on checkout (appointment only)", example = "video")
        String serviceType,

        @Schema(description = "Contact full name (appointment only)", example = "Nguyễn Minh Anh")
        String contactFullName,

        @Schema(description = "Contact email (appointment only)", example = "user@example.com")
        String contactEmail,

        @Schema(description = "Contact phone (appointment only)", example = "0912345678")
        String contactPhone
) {
}
