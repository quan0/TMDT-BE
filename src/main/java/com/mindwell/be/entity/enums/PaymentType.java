package com.mindwell.be.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum PaymentType {
    SUBSCRIPTION,
    MINDPOINTS,
    APPOINTMENT;

    @JsonValue
    public String toJson() {
        return name().toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static PaymentType fromJson(String value) {
        if (value == null) return null;
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "subscription" -> SUBSCRIPTION;
            case "mindpoints" -> MINDPOINTS;
            case "appointment" -> APPOINTMENT;
            default -> throw new IllegalArgumentException("Invalid paymentType: " + value);
        };
    }
}
