package com.mindwell.be.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(name = "SubscriptionPlan")
public record SubscriptionPlanDto(
        Integer subId,
        String name,
        BigDecimal price,
        @Schema(description = "Billing cycle", allowableValues = {"monthly", "yearly"})
        String billingCycle,
        String tierSubtitle,
        String badgeLabel,
        String shortDesc,
        List<String> features,
        Boolean isActive
) {
}
