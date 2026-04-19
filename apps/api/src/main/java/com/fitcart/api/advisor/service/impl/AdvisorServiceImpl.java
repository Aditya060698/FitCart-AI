package com.fitcart.api.advisor.service.impl;

import com.fitcart.api.advisor.dto.AdvisorProductRecommendationResponse;
import com.fitcart.api.advisor.dto.AdvisorQueryRequest;
import com.fitcart.api.advisor.dto.AdvisorResponse;
import com.fitcart.api.advisor.dto.ParsedAdvisorIntentResponse;
import com.fitcart.api.advisor.service.AdvisorQueryParser;
import com.fitcart.api.advisor.service.AdvisorService;
import com.fitcart.api.common.pagination.PageResponse;
import com.fitcart.api.personalization.dto.SearchHistoryRequest;
import com.fitcart.api.personalization.service.PersonalizationService;
import com.fitcart.api.product.dto.ProductFilterRequest;
import com.fitcart.api.product.dto.ProductSummaryResponse;
import com.fitcart.api.product.service.ProductService;
import com.fitcart.api.ranking.dto.RankingExplanationFactorResponse;
import com.fitcart.api.review.dto.ReviewAnalyticsResponse;
import com.fitcart.api.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdvisorServiceImpl implements AdvisorService {

    private final CachedAdvisorRecommendationService cachedAdvisorRecommendationService;
    private final AdvisorQueryParser advisorQueryParser;
    private final PersonalizationService personalizationService;
    private final ProductService productService;
    private final ReviewService reviewService;

    @Override
    public AdvisorResponse advise(AdvisorQueryRequest request) {
        ParsedAdvisorIntentResponse intent = parseIntentSafely(request.query());
        recordSearchHistoryIfPresent(request, intent);

        try {
            return cachedAdvisorRecommendationService.recommend(request);
        } catch (Exception exception) {
            log.warn("Advisor degraded for query '{}': {}", request.query(), exception.getMessage());
            return buildCatalogFallbackResponse(request, intent);
        }
    }

    private ParsedAdvisorIntentResponse parseIntentSafely(String query) {
        try {
            return advisorQueryParser.parse(query);
        } catch (Exception exception) {
            log.warn("Advisor parser degraded for query '{}': {}", query, exception.getMessage());
            return new ParsedAdvisorIntentResponse(
                    query == null ? "" : query.trim().toLowerCase(),
                    null,
                    null,
                    null,
                    null,
                    Set.of(),
                    Set.of()
            );
        }
    }

    private void recordSearchHistoryIfPresent(AdvisorQueryRequest request, ParsedAdvisorIntentResponse intent) {
        if (request.userReference() == null || request.userReference().isBlank()) {
            return;
        }

        try {
            personalizationService.recordSearchHistory(new SearchHistoryRequest(
                    request.userReference(),
                    request.query(),
                    intent.categoryHint(),
                    intent.goal()
            ));
        } catch (Exception exception) {
            log.warn("Search history recording degraded for user '{}': {}", request.userReference(), exception.getMessage());
        }
    }

    private AdvisorResponse buildCatalogFallbackResponse(AdvisorQueryRequest request, ParsedAdvisorIntentResponse intent) {
        int topK = request.topK() == null ? 3 : request.topK();

        PageResponse<ProductSummaryResponse> fallbackPage = productService.getProducts(
                new ProductFilterRequest(
                        null,
                        null,
                        request.query(),
                        true,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        Set.of()
                ),
                0,
                topK,
                "rating",
                "desc"
        );

        List<ProductSummaryResponse> products = fallbackPage.content();
        Map<Long, ReviewAnalyticsResponse> reviewAnalyticsByProductId = reviewService.getReviewAnalyticsByProductIds(
                products.stream().map(ProductSummaryResponse::id).toList()
        );

        List<AdvisorProductRecommendationResponse> recommendations = products.stream()
                .map(product -> toFallbackRecommendation(product, reviewAnalyticsByProductId.get(product.id())))
                .toList();

        return new AdvisorResponse(
                intent,
                recommendations,
                recommendations.isEmpty()
                        ? "AI explanation is temporarily unavailable, and no structured catalog matches were found."
                        : "AI explanation is temporarily unavailable. Showing structured catalog matches and review signals instead.",
                true,
                "CATALOG_SEARCH",
                "AI recommendation analysis degraded. Product search and comparison still work normally."
        );
    }

    private AdvisorProductRecommendationResponse toFallbackRecommendation(
            ProductSummaryResponse product,
            ReviewAnalyticsResponse reviewAnalytics
    ) {
        return new AdvisorProductRecommendationResponse(
                product.id(),
                product.name(),
                product.slug(),
                product.categoryName(),
                product.brandName(),
                product.price(),
                product.currencyCode(),
                product.proteinGrams(),
                product.sugarGrams(),
                product.ratingAverage(),
                product.dietaryFlags(),
                0.0,
                List.of(new RankingExplanationFactorResponse(
                        "catalogFallback",
                        1.0,
                        0.0,
                        0.0,
                        "Returned from structured catalog search because the AI recommendation layer degraded."
                )),
                reviewAnalytics == null ? "No review intelligence available yet." : reviewAnalytics.summaryContext(),
                reviewAnalytics == null ? "" : reviewAnalytics.commonKeywordsPlaceholder()
        );
    }
}
