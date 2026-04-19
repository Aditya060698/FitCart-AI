package com.fitcart.api.ranking.dto;

public record RankingExplanationFactorResponse(
        String factor,
        double rawScore,
        double weight,
        double weightedContribution,
        String explanation
) {
}
