package com.mindwell.be.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum AssessmentCategory {
    ANXIETY,
    DEPRESSION,
    STRESS,
    SLEEP,
    BURNOUT;

    @JsonValue
    public String toJson() {
        return name().toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static AssessmentCategory fromJson(String value) {
        if (value == null) return null;
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "anxiety" -> ANXIETY;
            case "depression" -> DEPRESSION;
            case "stress" -> STRESS;
            case "sleep" -> SLEEP;
            case "burnout" -> BURNOUT;
            default -> throw new IllegalArgumentException("Invalid assessment category: " + value);
        };
    }
}
