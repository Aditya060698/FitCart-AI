package com.fitcart.api.product.dto;

import java.math.BigDecimal;
import java.util.Set;

public record ProductFilterRequest(
        Long categoryId,
        Long brandId,
        String search,
        Boolean active,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Long goalId,
        BigDecimal minProteinGrams,
        BigDecimal maxProteinGrams,
        BigDecimal minSugarGrams,
        BigDecimal maxSugarGrams,
        BigDecimal minRating,
        Set<Long> dietaryFlagIds
) {
}
