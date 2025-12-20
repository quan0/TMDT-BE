package com.mindwell.be.dto.expert;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "ExpertFilterOptions")
public record ExpertFilterOptionsDto(
        List<LanguageOptionDto> languages,
        List<SpecializationOptionDto> specializations,
        @Schema(description = "Supported gender filter values")
        List<String> genders
) {
}
