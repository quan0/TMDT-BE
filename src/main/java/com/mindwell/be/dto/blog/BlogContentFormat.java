package com.mindwell.be.dto.blog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BlogContentFormat {
    MARKDOWN,
    HTML,
    PLAINTEXT;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static BlogContentFormat fromJson(String value) {
        if (value == null) return null;
        String normalized = value.trim().toUpperCase();
        return BlogContentFormat.valueOf(normalized);
    }
}
