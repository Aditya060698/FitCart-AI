package com.fitcart.api.personalization.dto;

import java.math.BigDecimal;
import java.util.Set;

public record PersonalizationContext(
        BigDecimal minBudget,
        BigDecimal maxBudget,
        Set<String> preferredBrands,
        Set<String> preferredCategories,
        Set<String> dietaryPreferences,
        Set<String> recentQueryTopics,
        Set<Long> savedProductIds
) {
}
