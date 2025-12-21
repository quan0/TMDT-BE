package com.mindwell.be.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    CANCELLED;

    @JsonValue
    public String toJson() {
        return name().toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static PaymentStatus fromJson(String value) {
        if (value == null) return null;
        String v = value.trim().toLowerCase(Locale.ROOT);
        return switch (v) {
            case "pending" -> PENDING;
            case "paid" -> PAID;
            case "failed" -> FAILED;
            case "cancelled", "canceled" -> CANCELLED;
            default -> throw new IllegalArgumentException("Invalid payment status: " + value);
        };
    }
}
