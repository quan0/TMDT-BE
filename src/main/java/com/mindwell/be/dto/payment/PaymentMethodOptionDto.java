package com.mindwell.be.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PaymentMethodOption")
public record PaymentMethodOptionDto(
        String methodKey,
        String displayName,
        String badgeLabel,
        Boolean isActive
) {
}
