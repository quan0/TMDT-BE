package com.mindwell.be.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum Gender {
    FEMALE,
    MALE,
    OTHER;

    @JsonValue
    public String toJson() {
        return name().toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static Gender fromJson(String value) {
        if (value == null) return null;
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "female" -> FEMALE;
            case "male" -> MALE;
            case "other" -> OTHER;
            default -> throw new IllegalArgumentException("Invalid gender: " + value);
        };
    }
}
