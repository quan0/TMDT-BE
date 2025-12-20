package com.mindwell.be.dto.expert;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SpecializationOption")
public record SpecializationOptionDto(
        @Schema(example = "1")
        Integer id,
        @Schema(example = "Anxiety")
        String name
) {
}
