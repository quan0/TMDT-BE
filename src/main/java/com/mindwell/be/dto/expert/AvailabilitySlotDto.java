package com.mindwell.be.dto.expert;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "AvailabilitySlot")
public record AvailabilitySlotDto(
        Integer availabilityId,
        @Schema(example = "2025-11-05T14:00:00")
        LocalDateTime startTime,
        @Schema(example = "2025-11-05T15:00:00")
        LocalDateTime endTime
) {
}
