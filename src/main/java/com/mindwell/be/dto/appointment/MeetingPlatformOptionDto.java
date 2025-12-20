package com.mindwell.be.dto.appointment;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MeetingPlatformOption")
public record MeetingPlatformOptionDto(
        Integer platformId,
        @Schema(example = "google_meet")
        String platformKey,
        @Schema(example = "Google Meet")
        String displayName,
        Boolean isActive
) {
}
