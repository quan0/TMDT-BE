package com.mindwell.be.dto.appointment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "CreateAppointmentResponse")
public record CreateAppointmentResponse(
        Integer apptId,
        Integer expertId,
        Integer userId,
        Integer availabilityId,
        LocalDateTime startTime,
        String status
) {
}
