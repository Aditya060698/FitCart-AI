package com.fitcart.api.ranking.service.impl;

import com.fitcart.api.dietaryflag.domain.entity.DietaryFlag;
import com.fitcart.api.goal.domain.entity.Goal;
import com.fitcart.api.personalization.dto.PersonalizationContext;
import com.fitcart.api.personalization.service.PersonalizationService;
import com.fitcart.api.product.domain.entity.Product;
import com.fitcart.api.product.repository.ProductRepository;
import com.fitcart.api.ranking.dto.RankProductsRequest;
import com.fitcart.api.ranking.dto.RankProductsResponse;
import com.fitcart.api.ranking.dto.RankedProductResponse;
import com.fitcart.api.ranking.dto.RankingCandidateRequest;
import com.fitcart.api.ranking.dto.RankingExplanationFactorResponse;
import com.fitcart.api.ranking.dto.RankingUserConstraints;
import com.fitcart.api.ranking.service.RankingService;
import com.fitcart.api.ranking.util.RankingScoreUtils;
import com.fitcart.api.review.domain.entity.ReviewAnalytics;
import com.fitcart.api.review.repository.ReviewAnalyticsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RankingServiceImpl implements RankingService {

    private static final double SEMANTIC_WEIGHT = 0.30;
    private static final double BUDGET_FIT_WEIGHT = 0.15;
    private static final double PROTEIN_QUALITY_WEIGHT = 0.12;
    private static final double SUGAR_PENALTY_WEIGHT = 0.10;
    private static final double RATING_WEIGHT = 0.10;
    private static final double REVIEW_SENTIMENT_WEIGHT = 0.10;
    private static final double POPULARITY_WEIGHT = 0.08;
    private static final double USER_PREFERENCE_WEIGHT = 0.15;
    private static final double BUDGET_PENALTY_WEIGHT = 0.05;

    private final ProductRepository productRepository;
    private final ReviewAnalyticsRepository reviewAnalyticsRepository;
    private final PersonalizationService personalizationService;

    @Override
    public RankProductsResponse rankProducts(RankProductsRequest request) {
        List<RankingCandidateRequest> candidates = request.candidates();
        int topK = request.topK() == null ? Math.min(10, candidates.size()) : request.topK();

        Map<Long, RankingCandidateRequest> candidateByProductId = candidates.stream()
                .collect(Collectors.toMap(RankingCandidateRequest::productId, Function.identity(), (left, right) -> left));

        List<Product> products = productRepository.findAllById(candidateByProductId.keySet());
        Map<Long, ReviewAnalytics> analyticsByProductId = reviewAnalyticsRepository.findAllByProductIdIn(candidateByProductId.keySet())
                .stream()
                .collect(Collectors.toMap(analytics -> analytics.getProduct().getId(), Function.identity()));
        PersonalizationContext personalizationContext = request.userConstraints() == null
                ? new PersonalizationContext(null, null, Set.of(), Set.of(), Set.of(), Set.of(), Set.of())
                : personalizationService.getContext(request.userConstraints().userReference());

        PriorityQueue<ScoredProductCandidate> heap = new PriorityQueue<>(Comparator.comparingDouble(ScoredProductCandidate::finalScore));

        for (Product product : products) {
            if (!product.isActive()) {
                continue;
            }

            RankingCandidateRequest candidateRequest = candidateByProductId.get(product.getId());
            ReviewAnalytics analytics = analyticsByProductId.get(product.getId());

            ScoredProductCandidate scoredCandidate = scoreCandidate(
                    product,
                    candidateRequest,
                    analytics,
                    request.userConstraints(),
                    personalizationContext
            );
            if (heap.size() < topK) {
                heap.offer(scoredCandidate);
                continue;
            }

            if (scoredCandidate.finalScore() > heap.peek().finalScore()) {
                heap.poll();
                heap.offer(scoredCandidate);
            }
        }

        List<ScoredProductCandidate> rankedCandidates = new ArrayList<>(heap);
        rankedCandidates.sort(Comparator.comparingDouble(ScoredProductCandidate::finalScore).reversed());

        List<RankedProductResponse> rankedProducts = rankedCandidates.stream()
                .map(this::toRankedProductResponse)
                .toList();

        return new RankProductsResponse(
                products.size(),
                rankedProducts.size(),
                rankedProducts
        );
    }

    private ScoredProductCandidate scoreCandidate(
            Product product,
            RankingCandidateRequest candidateRequest,
            ReviewAnalytics analytics,
            RankingUserConstraints userConstraints,
            PersonalizationContext personalizationContext
    ) {
        String normalizedGoal = normalize(userConstraints != null ? userConstraints.goal() : null);

        double semanticMatch = RankingScoreUtils.clamp(candidateRequest.semanticMatch() == null ? 0.5 : candidateRequest.semanticMatch());
        double budgetFit = calculateBudgetFit(product.getPrice(), userConstraints, personalizationContext);
        double proteinQuality = calculateProteinQuality(product);
        double sugarPenalty = calculateSugarPenalty(product, normalizedGoal);
        double ratingScore = calculateRatingScore(product, analytics);
        double reviewSentiment = calculateReviewSentiment(analytics);
        double popularity = RankingScoreUtils.clamp(candidateRequest.popularityScore() == null ? 0.5 : candidateRequest.popularityScore());
        double userPreferenceMatch = calculateUserPreferenceMatch(product, normalizedGoal, userConstraints, personalizationContext);
        double budgetPenalty = calculateBudgetPenalty(product.getPrice(), userConstraints, personalizationContext);

        List<RankingExplanationFactorResponse> explanationFactors = List.of(
                factor("semanticMatch", semanticMatch, SEMANTIC_WEIGHT, false, "Strong semantic match with the retrieved intent."),
                factor("budgetFit", budgetFit, BUDGET_FIT_WEIGHT, false, "Aligned against the user's target budget."),
                factor("proteinQuality", proteinQuality, PROTEIN_QUALITY_WEIGHT, false, "Derived from protein grams and product category fit."),
                factor("rating", ratingScore, RATING_WEIGHT, false, "Normalized product rating with review-confidence support."),
                factor("reviewSentiment", reviewSentiment, REVIEW_SENTIMENT_WEIGHT, false, "Derived from aggregated positive and negative review analytics."),
                factor("popularity", popularity, POPULARITY_WEIGHT, false, "Popularity boost passed from upstream retrieval or analytics."),
                factor("userPreferenceMatch", userPreferenceMatch, USER_PREFERENCE_WEIGHT, false, "Based on brand, dietary-flag, and goal alignment."),
                factor("sugarPenalty", sugarPenalty, SUGAR_PENALTY_WEIGHT, true, "Higher sugar receives a penalty when the goal suggests lower-sugar products."),
                factor("budgetPenalty", budgetPenalty, BUDGET_PENALTY_WEIGHT, true, "Products materially above budget receive a penalty.")
        );

        double finalScore = explanationFactors.stream()
                .mapToDouble(RankingExplanationFactorResponse::weightedContribution)
                .sum();

        return new ScoredProductCandidate(
                product,
                RankingScoreUtils.round(finalScore),
                explanationFactors.stream()
                        .sorted(Comparator.comparingDouble((RankingExplanationFactorResponse factor) -> Math.abs(factor.weightedContribution())).reversed())
                        .toList()
        );
    }

    private RankingExplanationFactorResponse factor(
            String name,
            double rawScore,
            double weight,
            boolean penalty,
            String explanation
    ) {
        double contribution = penalty ? -(rawScore * weight) : (rawScore * weight);
        return new RankingExplanationFactorResponse(
                name,
                RankingScoreUtils.round(rawScore),
                weight,
                RankingScoreUtils.round(contribution),
                explanation
        );
    }

    private double calculateBudgetFit(
            BigDecimal price,
            RankingUserConstraints constraints,
            PersonalizationContext personalizationContext
    ) {
        BigDecimal effectiveBudget = resolveEffectiveBudget(constraints, personalizationContext);
        if (effectiveBudget == null || effectiveBudget.compareTo(BigDecimal.ZERO) == 0) {
            return 0.5;
        }

        double budget = effectiveBudget.doubleValue();
        double productPrice = price.doubleValue();
        if (productPrice <= budget) {
            return 1.0;
        }

        double overshootRatio = (productPrice - budget) / budget;
        return RankingScoreUtils.clamp(1.0 - overshootRatio);
    }

    private double calculateBudgetPenalty(
            BigDecimal price,
            RankingUserConstraints constraints,
            PersonalizationContext personalizationContext
    ) {
        BigDecimal effectiveBudget = resolveEffectiveBudget(constraints, personalizationContext);
        if (effectiveBudget == null || effectiveBudget.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }

        double budget = effectiveBudget.doubleValue();
        double productPrice = price.doubleValue();
        if (productPrice <= budget) {
            return 0;
        }

        return RankingScoreUtils.clamp((productPrice - budget) / budget);
    }

    private double calculateProteinQuality(Product product) {
        if (product.getProteinGrams() == null) {
            return 0.35;
        }

        double normalizedProtein = RankingScoreUtils.clamp(product.getProteinGrams().doubleValue() / 30.0);
        String categoryName = normalize(product.getCategory().getName());
        if (categoryName.contains("protein")) {
            normalizedProtein = Math.min(1.0, normalizedProtein + 0.10);
        }

        return RankingScoreUtils.round(normalizedProtein);
    }

    private double calculateSugarPenalty(Product product, String normalizedGoal) {
        if (product.getSugarGrams() == null) {
            return 0;
        }

        double sugar = product.getSugarGrams().doubleValue();
        double threshold = 6.0;
        if (normalizedGoal.contains("cut")) {
            threshold = 4.0;
        } else if (normalizedGoal.contains("sleep")) {
            threshold = 3.0;
        } else if (normalizedGoal.contains("weight")) {
            threshold = 4.0;
        }

        if (sugar <= threshold) {
            return 0;
        }

        return RankingScoreUtils.clamp((sugar - threshold) / (threshold + 4.0));
    }

    private double calculateRatingScore(Product product, ReviewAnalytics analytics) {
        BigDecimal rating = analytics != null && analytics.getAverageRating() != null
                ? analytics.getAverageRating()
                : product.getRatingAverage();

        if (rating == null) {
            return 0.5;
        }

        double normalizedRating = RankingScoreUtils.clamp(rating.doubleValue() / 5.0);
        if (analytics == null || analytics.getTotalReviews() == null) {
            return normalizedRating;
        }

        double confidenceBoost = Math.min(analytics.getTotalReviews() / 100.0, 1.0) * 0.05;
        return RankingScoreUtils.clamp(normalizedRating + confidenceBoost);
    }

    private double calculateReviewSentiment(ReviewAnalytics analytics) {
        if (analytics == null || analytics.getTotalReviews() == null || analytics.getTotalReviews() == 0) {
            return 0.5;
        }

        double total = analytics.getTotalReviews();
        double positive = analytics.getPositiveReviewCount() == null ? 0 : analytics.getPositiveReviewCount();
        double negative = analytics.getNegativeReviewCount() == null ? 0 : analytics.getNegativeReviewCount();

        return RankingScoreUtils.clamp((positive + ((total - negative) * 0.5)) / total);
    }

    private double calculateUserPreferenceMatch(
            Product product,
            String normalizedGoal,
            RankingUserConstraints constraints,
            PersonalizationContext personalizationContext
    ) {
        if (constraints == null && personalizationContext == null) {
            return 0.5;
        }

        double score = 0;
        double maxScore = 0;

        Set<String> preferredBrands = new HashSet<>(normalizeSet(constraints == null ? null : constraints.preferredBrands()));
        if (personalizationContext != null) {
            preferredBrands.addAll(normalizeSet(personalizationContext.preferredBrands()));
        }
        if (!preferredBrands.isEmpty()) {
            maxScore += 0.4;
            if (preferredBrands.contains(normalize(product.getBrand().getName()))) {
                score += 0.4;
            }
        }

        Set<String> requiredDietaryFlags = new HashSet<>(normalizeSet(constraints == null ? null : constraints.requiredDietaryFlags()));
        if (personalizationContext != null) {
            requiredDietaryFlags.addAll(normalizeSet(personalizationContext.dietaryPreferences()));
        }
        if (!requiredDietaryFlags.isEmpty()) {
            maxScore += 0.4;
            Set<String> productDietaryFlags = product.getDietaryFlags().stream()
                    .map(DietaryFlag::getName)
                    .map(this::normalize)
                    .collect(Collectors.toSet());

            long matchedFlags = requiredDietaryFlags.stream()
                    .filter(productDietaryFlags::contains)
                    .count();
            score += (matchedFlags / (double) requiredDietaryFlags.size()) * 0.4;
        }

        if (!normalizedGoal.isBlank()) {
            maxScore += 0.2;
            boolean goalMatched = product.getGoals().stream()
                    .map(Goal::getName)
                    .map(this::normalize)
                    .anyMatch(goalName -> goalName.contains(normalizedGoal) || normalizedGoal.contains(goalName));
            if (goalMatched) {
                score += 0.2;
            }
        }

        if (personalizationContext != null && personalizationContext.savedProductIds().contains(product.getId())) {
            maxScore += 0.1;
            score += 0.1;
        }

        if (personalizationContext != null && !personalizationContext.preferredCategories().isEmpty()) {
            maxScore += 0.15;
            if (normalizeSet(personalizationContext.preferredCategories()).contains(normalize(product.getCategory().getName()))) {
                score += 0.15;
            }
        }

        if (personalizationContext != null && !personalizationContext.recentQueryTopics().isEmpty()) {
            maxScore += 0.15;
            Set<String> productTokens = new HashSet<>();
            productTokens.addAll(tokenize(product.getName()));
            productTokens.addAll(tokenize(product.getCategory().getName()));
            productTokens.addAll(tokenize(product.getShortDescription()));

            long topicMatches = normalizeSet(personalizationContext.recentQueryTopics()).stream()
                    .filter(productTokens::contains)
                    .count();
            score += Math.min(topicMatches / 3.0, 1.0) * 0.15;
        }

        if (maxScore == 0) {
            return 0.5;
        }

        return RankingScoreUtils.clamp(score / maxScore);
    }

    private RankedProductResponse toRankedProductResponse(ScoredProductCandidate candidate) {
        Product product = candidate.product();
        Set<String> dietaryFlags = product.getDietaryFlags().stream()
                .map(DietaryFlag::getName)
                .collect(Collectors.toCollection(HashSet::new));

        return new RankedProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getCategory().getName(),
                product.getBrand().getName(),
                product.getPrice(),
                product.getCurrencyCode(),
                product.getProteinGrams(),
                product.getSugarGrams(),
                product.getRatingAverage(),
                dietaryFlags,
                candidate.finalScore(),
                candidate.explanationFactors()
        );
    }

    private Set<String> normalizeSet(Collection<String> values) {
        if (values == null) {
            return Set.of();
        }

        return values.stream()
                .map(this::normalize)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toSet());
    }

    private Set<String> tokenize(String value) {
        return Arrays.stream(normalize(value).split("\\s+"))
                .filter(token -> !token.isBlank())
                .collect(Collectors.toSet());
    }

    private BigDecimal resolveEffectiveBudget(
            RankingUserConstraints constraints,
            PersonalizationContext personalizationContext
    ) {
        if (constraints != null && constraints.budget() != null) {
            return constraints.budget();
        }
        if (personalizationContext != null && personalizationContext.maxBudget() != null) {
            return personalizationContext.maxBudget();
        }
        return null;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private record ScoredProductCandidate(
            Product product,
            double finalScore,
            List<RankingExplanationFactorResponse> explanationFactors
    ) {
    }
}
