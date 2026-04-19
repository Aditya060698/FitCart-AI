package com.fitcart.api.ranking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

public record RankingUserConstraints(
        @DecimalMin("0.0") BigDecimal budget,
        @Size(max = 120) String goal,
        Set<String> preferredBrands,
        Set<String> requiredDietaryFlags,
        @Size(max = 120) String userReference
) {
}
