package com.fitcart.api.personalization.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

public record UserPreferenceProfileRequest(
        @NotBlank String userReference,
        @DecimalMin("0.0") BigDecimal minBudget,
        @DecimalMin("0.0") BigDecimal maxBudget,
        @Size(max = 120) String primaryGoal,
        Set<String> dietaryPreferences
) {
}
