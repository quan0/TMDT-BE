package com.mindwell.be.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum SenderType {
    USER,
    EXPERT;

    @JsonValue
    public String toJson() {
        return name().toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static SenderType fromJson(String value) {
        if (value == null) return null;
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "user" -> USER;
            case "expert" -> EXPERT;
            default -> throw new IllegalArgumentException("Invalid senderType: " + value);
        };
    }
}
