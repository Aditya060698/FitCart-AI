package com.fitcart.api.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
        @NotNull
        Long productId,

        @Size(max = 80)
        String source,

        @Size(max = 120)
        String externalReviewId,

        @Size(max = 150)
        String reviewerName,

        @NotNull
        @Min(1)
        @Max(5)
        Integer rating,

        @Size(max = 255)
        String reviewTitle,

        @NotBlank
        String reviewBody,

        boolean verifiedPurchase,

        @Size(max = 30)
        String sentimentLabel
) {
}
