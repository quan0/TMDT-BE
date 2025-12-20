package com.mindwell.be.dto.appointment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "AppointmentOptions")
public record AppointmentOptionsDto(
        @Schema(description = "Meeting platforms available in DB")
        List<MeetingPlatformOptionDto> platforms,
        @Schema(description = "Distinct service types present in DB")
        List<String> serviceTypes
) {
}
