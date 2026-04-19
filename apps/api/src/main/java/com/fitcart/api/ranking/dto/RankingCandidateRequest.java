package com.fitcart.api.ranking.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record RankingCandidateRequest(
        @NotNull Long productId,
        @DecimalMin("0.0") @DecimalMax("1.0") Double semanticMatch,
        @DecimalMin("0.0") @DecimalMax("1.0") Double popularityScore
) {
}
