package com.mindwell.be.service.spec;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ExpertSearchCriteria(
        String q,
        List<Integer> specializationIds,
        List<String> languageCodes,
        String gender,
        Boolean verified,
        BigDecimal minRate,
        BigDecimal maxRate,
        LocalDateTime availableFrom,
        LocalDateTime availableTo
) {
}
