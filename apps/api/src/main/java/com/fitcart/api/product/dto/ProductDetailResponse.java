package com.fitcart.api.product.dto;

import com.fitcart.api.review.dto.ReviewResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record ProductDetailResponse(
        Long id,
        String sku,
        String name,
        String slug,
        String description,
        String shortDescription,
        BigDecimal price,
        String currencyCode,
        BigDecimal proteinGrams,
        BigDecimal sugarGrams,
        BigDecimal ratingAverage,
        boolean active,
        ReferenceItem brand,
        ReferenceItem category,
        Set<ReferenceItem> goals,
        Set<ReferenceItem> dietaryFlags,
        List<ReviewResponse> reviews
) {
    public record ReferenceItem(
            Long id,
            String name
    ) {
    }
}
