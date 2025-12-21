package com.mindwell.be.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum AppointmentServiceType {
    VIDEO,
    CHAT,
    VOICE;

    @JsonValue
    public String toJson() {
        return name().toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static AppointmentServiceType fromJson(String value) {
        if (value == null) return null;
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "video" -> VIDEO;
            case "chat" -> CHAT;
            case "voice" -> VOICE;
            default -> throw new IllegalArgumentException("Invalid serviceType: " + value);
        };
    }
}
