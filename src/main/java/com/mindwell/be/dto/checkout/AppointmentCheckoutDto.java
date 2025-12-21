package com.mindwell.be.dto.checkout;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "AppointmentCheckout")
public record AppointmentCheckoutDto(
        Integer apptId,
        Integer expertId,
        String expertName,
        String expertTitle,
        Integer platformId,
        String platformName,
        String serviceType,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer totalAmountPoints,
        Integer userMindpointsBalance,
        String contactFullName,
        String contactEmail,
        String contactPhone,
        String status
) {
}
