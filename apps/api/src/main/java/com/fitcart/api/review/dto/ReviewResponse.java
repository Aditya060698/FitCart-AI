package com.fitcart.api.review.dto;

import java.time.OffsetDateTime;

public record ReviewResponse(
        Long id,
        Long productId,
        String productName,
        String reviewerName,
        Integer rating,
        String reviewTitle,
        String reviewBody,
        boolean verifiedPurchase,
        String sentimentLabel,
        OffsetDateTime submittedAt
) {
}
