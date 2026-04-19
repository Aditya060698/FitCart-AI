package com.fitcart.api.review.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

public record ReviewAnalyticsResponse(
        Long productId,
        String productName,
        Integer totalReviews,
        BigDecimal averageRating,
        Map<Integer, Integer> ratingDistribution,
        Integer verifiedPurchaseCount,
        Integer positiveReviewCount,
        Integer neutralReviewCount,
        Integer negativeReviewCount,
        OffsetDateTime latestReviewSubmittedAt,
        String commonKeywordsPlaceholder,
        String summaryContext,
        boolean summaryReady,
        OffsetDateTime lastAggregatedAt
) {
}
