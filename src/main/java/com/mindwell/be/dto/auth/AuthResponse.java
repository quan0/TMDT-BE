package com.mindwell.be.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthResponse")
public record AuthResponse(
        @Schema(example = "Bearer")
        String tokenType,

        @Schema(description = "JWT access token")
        String accessToken,

        Integer userId,
        String email,
        String fullName
) {
}
