package com.fitcart.api.advisor.service.impl;

import com.fitcart.api.advisor.dto.ParsedAdvisorIntentResponse;
import com.fitcart.api.advisor.service.AdvisorRetrievalService;
import com.fitcart.api.common.pagination.PageResponse;
import com.fitcart.api.goal.domain.entity.Goal;
import com.fitcart.api.goal.repository.GoalRepository;
import com.fitcart.api.product.dto.ProductFilterRequest;
import com.fitcart.api.product.dto.ProductSummaryResponse;
import com.fitcart.api.product.service.ProductService;
import com.fitcart.api.ranking.dto.RankingCandidateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductAdvisorRetrievalService implements AdvisorRetrievalService {

    private final ProductService productService;
    private final GoalRepository goalRepository;

    @Override
    public List<RankingCandidateRequest> retrieveCandidates(ParsedAdvisorIntentResponse intent, int topK) {
        ProductFilterRequest filter = new ProductFilterRequest(
                null,
                null,
                buildSearchQuery(intent),
                true,
                null,
                intent.budget(),
                resolveGoalId(intent.goal()),
                null,
                null,
                null,
                intent.maxSugarGrams(),
                null,
                null
        );

        PageResponse<ProductSummaryResponse> page = productService.getProducts(filter, 0, Math.max(topK * 4, 12), "rating", "desc");

        return page.content().stream()
                .map(product -> new RankingCandidateRequest(
                        product.id(),
                        calculateSemanticMatch(product, intent),
                        calculatePopularityScore(product)
                ))
                .sorted(Comparator.comparingDouble(RankingCandidateRequest::semanticMatch).reversed())
                .limit(Math.max(topK * 3L, 6L))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String buildSearchQuery(ParsedAdvisorIntentResponse intent) {
        Set<String> tokens = intent.preferenceSignals();
        if (tokens == null || tokens.isEmpty()) {
            return intent.normalizedQuery();
        }

        return tokens.stream()
                .filter(token -> !token.isBlank())
                .collect(Collectors.joining(" "));
    }

    private Long resolveGoalId(String goal) {
        if (goal == null || goal.isBlank()) {
            return null;
        }

        String normalizedGoal = normalize(goal);
        return goalRepository.findAll().stream()
                .filter(candidate -> normalize(candidate.getName()).contains(normalizedGoal)
                        || normalizedGoal.contains(normalize(candidate.getName()))
                        || normalize(candidate.getSlug()).contains(normalizedGoal.replace(" ", "-")))
                .map(Goal::getId)
                .findFirst()
                .orElse(null);
    }

    private double calculateSemanticMatch(ProductSummaryResponse product, ParsedAdvisorIntentResponse intent) {
        List<String> queryTokens = tokenize(intent.normalizedQuery());
        List<String> documentTokens = tokenize(
                "%s %s %s %s".formatted(
                        product.name(),
                        product.categoryName(),
                        product.brandName(),
                        product.shortDescription() == null ? "" : product.shortDescription()
                )
        );

        if (queryTokens.isEmpty() || documentTokens.isEmpty()) {
            return 0.5;
        }

        long overlap = queryTokens.stream()
                .filter(documentTokens::contains)
                .count();

        double score = Math.min(overlap / (double) queryTokens.size(), 1.0);

        if (intent.categoryHint() != null && normalize(product.categoryName()).contains(normalize(intent.categoryHint()))) {
            score += 0.15;
        }
        if (intent.goal() != null && normalize(product.shortDescription() == null ? "" : product.shortDescription()).contains(normalize(intent.goal()))) {
            score += 0.10;
        }
        if (intent.maxSugarGrams() != null && product.sugarGrams() != null && product.sugarGrams().compareTo(intent.maxSugarGrams()) <= 0) {
            score += 0.10;
        }

        return Math.min(score, 1.0);
    }

    private double calculatePopularityScore(ProductSummaryResponse product) {
        BigDecimal rating = product.ratingAverage();
        if (rating == null) {
            return 0.5;
        }
        return Math.min(rating.doubleValue() / 5.0, 1.0);
    }

    private List<String> tokenize(String text) {
        return List.of(normalize(text).split("\\s+")).stream()
                .filter(token -> !token.isBlank())
                .toList();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
