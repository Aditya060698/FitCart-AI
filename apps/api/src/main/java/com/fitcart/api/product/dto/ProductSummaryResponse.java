package com.fitcart.api.product.dto;

import java.math.BigDecimal;
import java.util.Set;

public record ProductSummaryResponse(
        Long id,
        String name,
        String slug,
        String categoryName,
        String brandName,
        BigDecimal price,
        String currencyCode,
        String shortDescription,
        BigDecimal proteinGrams,
        BigDecimal sugarGrams,
        BigDecimal ratingAverage,
        Set<String> dietaryFlags
) {
}
