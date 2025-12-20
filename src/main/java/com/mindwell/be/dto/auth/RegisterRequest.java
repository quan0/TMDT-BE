package com.mindwell.be.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RegisterRequest")
public record RegisterRequest(
        @Email
        @NotBlank
        @Schema(example = "newuser@example.com")
        String email,

        @NotBlank
        @Size(min = 6, max = 72)
        @Schema(example = "password123")
        String password,

        @NotBlank
        @Size(max = 120)
        @Schema(example = "Nguyễn Văn A")
        String fullName,

        @Size(max = 30)
        @Schema(example = "0901234567")
        String phoneNumber
) {
}
