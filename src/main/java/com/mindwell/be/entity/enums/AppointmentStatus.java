package com.mindwell.be.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum AppointmentStatus {
    DRAFT,
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    PENDING_PROVIDER_PAYMENT;

    @JsonValue
    public String toJson() {
        return name().toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static AppointmentStatus fromJson(String value) {
        if (value == null) return null;
        String v = value.trim().toLowerCase(Locale.ROOT);
        return switch (v) {
            case "draft" -> DRAFT;
            case "pending" -> PENDING;
            case "confirmed" -> CONFIRMED;
            case "completed" -> COMPLETED;
            case "cancelled", "canceled" -> CANCELLED;
            case "pending_provider_payment" -> PENDING_PROVIDER_PAYMENT;
            default -> throw new IllegalArgumentException("Invalid appointment status: " + value);
        };
    }
}
