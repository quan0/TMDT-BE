package com.mindwell.be.dto.expert;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "ExpertReview")
public record ExpertReviewDto(
        Integer reviewId,
        Integer rating,
        String comment,
        Integer userId,
        String userFullName,
        @Schema(description = "Appointment start time associated with this review")
        LocalDateTime appointmentStartTime
) {
}
