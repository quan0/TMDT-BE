package com.mindwell.be.dto.expert;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LanguageOption")
public record LanguageOptionDto(
        @Schema(example = "en")
        String code,
        @Schema(example = "English")
        String name
) {
}
