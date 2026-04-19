package com.fitcart.api.advisor.dto;

import java.math.BigDecimal;
import java.util.Set;

public record ParsedAdvisorIntentResponse(
        String normalizedQuery,
        String categoryHint,
        String goal,
        BigDecimal budget,
        BigDecimal maxSugarGrams,
        Set<String> preferenceSignals,
        Set<String> requiredDietaryFlags
) {
}
