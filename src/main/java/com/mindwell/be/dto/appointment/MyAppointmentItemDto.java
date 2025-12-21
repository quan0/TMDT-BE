package com.mindwell.be.dto.appointment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "MyAppointmentItem")
public record MyAppointmentItemDto(
        Integer apptId,
        Integer expertId,
        String expertName,
        String expertTitle,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,
        String serviceType,
        Integer platformId,
        String platformName,
        Integer totalAmountPoints,
        Integer paymentId,
        String paymentStatus,
        String meetingJoinUrl
) {
}
