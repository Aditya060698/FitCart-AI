package com.fitcart.api.advisor.service.impl;

import com.fitcart.api.advisor.dto.AdvisorProductRecommendationResponse;
import com.fitcart.api.advisor.dto.ParsedAdvisorIntentResponse;
import com.fitcart.api.advisor.service.AdvisorAnswerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroundedAdvisorAnswerService implements AdvisorAnswerService {

    @Override
    public String generateAnswer(ParsedAdvisorIntentResponse intent, List<AdvisorProductRecommendationResponse> recommendations) {
        if (recommendations.isEmpty()) {
            return "I could not find a grounded product recommendation that matches the current constraints. Try relaxing the budget, sugar requirement, or category wording.";
        }

        String intro = "Based on your query for %s, the strongest grounded matches are %s."
                .formatted(intent.normalizedQuery(), joinNames(recommendations));

        String details = recommendations.stream()
                .limit(3)
                .map(recommendation -> {
                    String reviewEvidence = recommendation.reviewSummary() == null || recommendation.reviewSummary().isBlank()
                            ? "Review intelligence is limited for this product."
                            : recommendation.reviewSummary();

                    return "%s stands out because it costs %s %s, has %s g protein, %s g sugar, and a rating of %s. %s"
                            .formatted(
                                    recommendation.name(),
                                    recommendation.currencyCode(),
                                    recommendation.price(),
                                    recommendation.proteinGrams() == null ? "N/A" : recommendation.proteinGrams(),
                                    recommendation.sugarGrams() == null ? "N/A" : recommendation.sugarGrams(),
                                    recommendation.ratingAverage() == null ? "N/A" : recommendation.ratingAverage(),
                                    reviewEvidence
                            );
                })
                .collect(Collectors.joining(" "));

        return intro + " " + details + " This answer is grounded in catalog filters, ranking signals, and review analytics rather than free-form product claims.";
    }

    private String joinNames(List<AdvisorProductRecommendationResponse> recommendations) {
        return recommendations.stream()
                .limit(3)
                .map(AdvisorProductRecommendationResponse::name)
                .collect(Collectors.joining(", "));
    }
}
