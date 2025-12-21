package com.mindwell.be.dto.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "CreateAppointmentDraftRequest")
public record CreateAppointmentDraftRequest(
        @NotNull
        @Schema(description = "Selected availability slot id", example = "123")
        Integer availabilityId
) {
}
