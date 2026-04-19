package com.fitcart.api.advisor.dto;

import java.util.List;

public record AdvisorResponse(
        ParsedAdvisorIntentResponse parsedIntent,
        List<AdvisorProductRecommendationResponse> recommendations,
        String answer,
        boolean degraded,
        String fallbackMode,
        String notice
) {
}
