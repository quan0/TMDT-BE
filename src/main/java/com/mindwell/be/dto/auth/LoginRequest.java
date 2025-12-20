package com.mindwell.be.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest")
public record LoginRequest(
        @Email
        @NotBlank
        @Schema(example = "newuser@example.com")
        String email,

        @NotBlank
        @Schema(example = "password123")
        String password
) {
}
