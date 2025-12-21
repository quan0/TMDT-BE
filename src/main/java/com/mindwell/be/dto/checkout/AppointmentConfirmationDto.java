package com.mindwell.be.dto.checkout;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "AppointmentConfirmation")
public record AppointmentConfirmationDto(
        Integer apptId,
        String status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String platformName,
        String meetingJoinUrl,
        String contactEmail
) {
}
