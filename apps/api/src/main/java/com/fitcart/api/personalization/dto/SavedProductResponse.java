package com.fitcart.api.personalization.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

public record SavedProductResponse(
        Long id,
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
        OffsetDateTime savedAt
) {
}
