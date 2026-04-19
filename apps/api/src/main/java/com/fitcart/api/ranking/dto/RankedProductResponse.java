package com.fitcart.api.ranking.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record RankedProductResponse(
        Long productId,
        String name,
        String slug,
        String categoryName,
        String brandName,
        BigDecimal price,
        String currencyCode,
        BigDecimal proteinGrams,
        BigDecimal sugarGrams,
        BigDecimal ratingAverage,
        Set<String> dietaryFlags,
        double finalScore,
        List<RankingExplanationFactorResponse> explanationFactors
) {
}
