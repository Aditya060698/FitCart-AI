package com.fitcart.api.advisor.service.impl;

import com.fitcart.api.advisor.dto.AdvisorProductRecommendationResponse;
import com.fitcart.api.advisor.dto.AdvisorQueryRequest;
import com.fitcart.api.advisor.dto.AdvisorResponse;
import com.fitcart.api.advisor.dto.ParsedAdvisorIntentResponse;
import com.fitcart.api.advisor.service.AdvisorAnswerService;
import com.fitcart.api.advisor.service.AdvisorQueryParser;
import com.fitcart.api.advisor.service.AdvisorRetrievalService;
import com.fitcart.api.common.cache.CacheKeyBuilder;
import com.fitcart.api.common.cache.CacheNames;
import com.fitcart.api.ranking.dto.RankProductsRequest;
import com.fitcart.api.ranking.dto.RankProductsResponse;
import com.fitcart.api.ranking.dto.RankedProductResponse;
import com.fitcart.api.ranking.dto.RankingUserConstraints;
import com.fitcart.api.ranking.service.RankingService;
import com.fitcart.api.review.dto.ReviewAnalyticsResponse;
import com.fitcart.api.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CachedAdvisorRecommendationService {

    private final AdvisorQueryParser advisorQueryParser;
    private final AdvisorRetrievalService advisorRetrievalService;
    private final RankingService rankingService;
    private final ReviewService reviewService;
    private final AdvisorAnswerService advisorAnswerService;

    @Cacheable(
            cacheNames = CacheNames.ADVISOR_TOP_PRODUCTS,
            key = "T(com.fitcart.api.common.cache.CacheKeyBuilder).advisorQuery(#request.query(), #request.topK())",
            condition = "#request.userReference() == null || #request.userReference().isBlank()"
    )
    public AdvisorResponse recommend(AdvisorQueryRequest request) {
        int topK = request.topK() == null ? 3 : request.topK();

        ParsedAdvisorIntentResponse intent = advisorQueryParser.parse(request.query());
        RankProductsResponse rankedProducts = rankingService.rankProducts(new RankProductsRequest(
                advisorRetrievalService.retrieveCandidates(intent, topK),
                new RankingUserConstraints(
                        intent.budget(),
                        intent.goal(),
                        Set.of(),
                        intent.requiredDietaryFlags(),
                        request.userReference()
                ),
                topK
        ));

        Map<Long, ReviewAnalyticsResponse> reviewAnalyticsByProductId = reviewService.getReviewAnalyticsByProductIds(
                rankedProducts.products().stream()
                        .map(RankedProductResponse::productId)
                        .toList()
        );

        List<AdvisorProductRecommendationResponse> recommendations = rankedProducts.products().stream()
                .map(product -> toAdvisorRecommendation(product, reviewAnalyticsByProductId.get(product.productId())))
                .collect(Collectors.toList());

        return new AdvisorResponse(
                intent,
                recommendations,
                advisorAnswerService.generateAnswer(intent, recommendations),
                false,
                null,
                null
        );
    }

    private AdvisorProductRecommendationResponse toAdvisorRecommendation(
            RankedProductResponse product,
            ReviewAnalyticsResponse reviewAnalytics
    ) {
        return new AdvisorProductRecommendationResponse(
                product.productId(),
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
                product.finalScore(),
                product.explanationFactors(),
                reviewAnalytics == null ? "No review intelligence available yet." : reviewAnalytics.summaryContext(),
                reviewAnalytics == null ? "" : reviewAnalytics.commonKeywordsPlaceholder()
        );
    }
}
