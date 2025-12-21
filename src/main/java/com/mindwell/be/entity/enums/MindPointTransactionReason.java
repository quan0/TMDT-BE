package com.mindwell.be.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum MindPointTransactionReason {
    PURCHASE,
    APPOINTMENT_PAYMENT;

    @JsonValue
    public String toJson() {
        return name().toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static MindPointTransactionReason fromJson(String value) {
        if (value == null) return null;
        String v = value.trim().toLowerCase(Locale.ROOT);
        return switch (v) {
            case "purchase" -> PURCHASE;
            case "appointment_payment", "appointment payment" -> APPOINTMENT_PAYMENT;
            default -> throw new IllegalArgumentException("Invalid transaction reason: " + value);
        };
    }
}
