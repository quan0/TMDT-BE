package com.mindwell.be.dto.expert;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(name = "ExpertDetail")
public record ExpertDetailDto(
        Integer expertId,
        String fullName,
        String title,
        BigDecimal hourlyRate,
        Boolean isVerified,
        String gender,
        @Schema(description = "Average rating from reviews", example = "4.8")
        Double avgRating,
        @Schema(description = "Number of reviews", example = "127")
        Long reviewCount,
        List<LanguageOptionDto> languages,
        List<SpecializationOptionDto> specializations
) {
}
