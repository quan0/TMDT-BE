package com.mindwell.be.dto.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "CreateAppointmentRequest")
public record CreateAppointmentRequest(
        @NotNull
        @Schema(description = "Selected availability slot id", example = "123")
        Integer availabilityId,

        @Schema(description = "video|chat|voice", example = "video")
        String serviceType,

        @Schema(description = "Meeting platform id (optional)", example = "1")
        Integer platformId
) {
}
