package com.fitcart.api.personalization.dto;

import java.math.BigDecimal;
import java.util.Set;

public record UserPreferenceProfileResponse(
        String userReference,
        BigDecimal minBudget,
        BigDecimal maxBudget,
        String primaryGoal,
        Set<String> dietaryPreferences
) {
}
