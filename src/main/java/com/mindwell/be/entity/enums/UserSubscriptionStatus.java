package com.mindwell.be.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum UserSubscriptionStatus {
    ACTIVE,
    EXPIRED,
    CANCELLED;

    @JsonValue
    public String toJson() {
        return name().toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static UserSubscriptionStatus fromJson(String value) {
        if (value == null) return null;
        String v = value.trim().toLowerCase(Locale.ROOT);
        return switch (v) {
            case "active" -> ACTIVE;
            case "expired" -> EXPIRED;
            case "cancelled", "canceled" -> CANCELLED;
            default -> throw new IllegalArgumentException("Invalid subscription status: " + value);
        };
    }
}
