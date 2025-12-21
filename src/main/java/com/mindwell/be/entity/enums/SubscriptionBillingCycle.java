package com.mindwell.be.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum SubscriptionBillingCycle {
    MONTHLY,
    YEARLY;

    @JsonValue
    public String toJson() {
        return name().toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static SubscriptionBillingCycle fromJson(String value) {
        if (value == null) return null;
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "monthly" -> MONTHLY;
            case "yearly", "annual", "year" -> YEARLY;
            default -> throw new IllegalArgumentException("Invalid billingCycle: " + value);
        };
    }
}
