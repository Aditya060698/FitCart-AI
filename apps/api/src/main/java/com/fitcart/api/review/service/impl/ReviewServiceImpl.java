package com.fitcart.api.review.service.impl;

import com.fitcart.api.product.domain.entity.Product;
import com.fitcart.api.product.repository.ProductRepository;
import com.fitcart.api.common.cache.CacheNames;
import com.fitcart.api.review.domain.entity.Review;
import com.fitcart.api.review.domain.entity.ReviewAnalytics;
import com.fitcart.api.review.dto.CreateReviewRequest;
import com.fitcart.api.review.dto.ReviewAnalyticsResponse;
import com.fitcart.api.review.dto.ReviewResponse;
import com.fitcart.api.review.mapper.ReviewMapper;
import com.fitcart.api.review.repository.ReviewAnalyticsRepository;
import com.fitcart.api.review.repository.ReviewRepository;
import com.fitcart.api.review.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewAnalyticsRepository reviewAnalyticsRepository;
    private final ProductRepository productRepository;
    private final ReviewMapper reviewMapper;

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "with", "this", "that", "from", "into", "your", "you",
            "are", "was", "were", "have", "has", "had", "very", "good", "great", "nice",
            "but", "not", "too", "its", "it's", "about", "just", "more", "less", "than",
            "after", "before", "they", "them", "their", "really", "solid", "used"
    );

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.ADVISOR_TOP_PRODUCTS, allEntries = true)
    public ReviewResponse createReview(CreateReviewRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found for id " + request.productId()));

        Review review = Review.builder()
                .product(product)
                .source(request.source())
                .externalReviewId(request.externalReviewId())
                .reviewerName(request.reviewerName())
                .rating(request.rating())
                .reviewTitle(request.reviewTitle())
                .reviewBody(request.reviewBody())
                .verifiedPurchase(request.verifiedPurchase())
                .sentimentLabel(request.sentimentLabel())
                .build();

        Review saved = reviewRepository.save(review);
        refreshReviewAnalytics(product);
        return reviewMapper.toResponse(saved);
    }

    @Override
    public List<ReviewResponse> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductIdOrderBySubmittedAtDesc(productId)
                .stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    @Override
    public ReviewAnalyticsResponse getReviewAnalyticsByProduct(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found for id " + productId));

        return reviewAnalyticsRepository.findByProductId(productId)
                .map(reviewMapper::toAnalyticsResponse)
                .orElseGet(() -> reviewMapper.toAnalyticsResponse(buildEmptyAnalytics(productId)));
    }

    @Override
    public Map<Long, ReviewAnalyticsResponse> getReviewAnalyticsByProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, ReviewAnalyticsResponse> analyticsMap = reviewAnalyticsRepository.findAllByProductIdIn(productIds)
                .stream()
                .map(reviewMapper::toAnalyticsResponse)
                .collect(Collectors.toMap(ReviewAnalyticsResponse::productId, analytics -> analytics));

        for (Long productId : productIds) {
            analyticsMap.computeIfAbsent(productId, missingId -> reviewMapper.toAnalyticsResponse(buildEmptyAnalytics(missingId)));
        }

        return analyticsMap;
    }

    private void refreshReviewAnalytics(Product product) {
        List<Review> reviews = reviewRepository.findByProductId(product.getId());

        ReviewAnalytics analytics = reviewAnalyticsRepository.findByProductId(product.getId())
                .orElseGet(() -> ReviewAnalytics.builder().product(product).build());

        int totalReviews = reviews.size();
        Map<Integer, Integer> distribution = initializeDistribution();
        int verifiedPurchaseCount = 0;
        int positiveReviewCount = 0;
        int neutralReviewCount = 0;
        int negativeReviewCount = 0;

        OffsetDateTime latestReviewSubmittedAt = reviews.stream()
                .map(Review::getSubmittedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);

        int ratingSum = 0;

        for (Review review : reviews) {
            int rating = review.getRating();
            distribution.computeIfPresent(rating, (key, value) -> value + 1);
            ratingSum += rating;

            if (review.isVerifiedPurchase()) {
                verifiedPurchaseCount++;
            }

            if (rating >= 4) {
                positiveReviewCount++;
            } else if (rating <= 2) {
                negativeReviewCount++;
            } else {
                neutralReviewCount++;
            }
        }

        BigDecimal averageRating = totalReviews == 0
                ? null
                : BigDecimal.valueOf(ratingSum)
                .divide(BigDecimal.valueOf(totalReviews), 2, RoundingMode.HALF_UP);

        String commonKeywords = buildCommonKeywordsPlaceholder(reviews);
        String summaryContext = buildSummaryContext(product, totalReviews, averageRating, commonKeywords, positiveReviewCount, negativeReviewCount);

        analytics.setTotalReviews(totalReviews);
        analytics.setAverageRating(averageRating);
        analytics.setOneStarCount(distribution.get(1));
        analytics.setTwoStarCount(distribution.get(2));
        analytics.setThreeStarCount(distribution.get(3));
        analytics.setFourStarCount(distribution.get(4));
        analytics.setFiveStarCount(distribution.get(5));
        analytics.setVerifiedPurchaseCount(verifiedPurchaseCount);
        analytics.setPositiveReviewCount(positiveReviewCount);
        analytics.setNeutralReviewCount(neutralReviewCount);
        analytics.setNegativeReviewCount(negativeReviewCount);
        analytics.setLatestReviewSubmittedAt(latestReviewSubmittedAt);
        analytics.setCommonKeywordsPlaceholder(commonKeywords);
        analytics.setSummaryContext(summaryContext);
        analytics.setSummaryReady(totalReviews >= 3);
        analytics.setLastAggregatedAt(OffsetDateTime.now());

        reviewAnalyticsRepository.save(analytics);

        product.setRatingAverage(averageRating);
        productRepository.save(product);
    }

    private ReviewAnalytics buildEmptyAnalytics(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found for id " + productId));

        return ReviewAnalytics.builder()
                .product(product)
                .totalReviews(0)
                .averageRating(null)
                .oneStarCount(0)
                .twoStarCount(0)
                .threeStarCount(0)
                .fourStarCount(0)
                .fiveStarCount(0)
                .verifiedPurchaseCount(0)
                .positiveReviewCount(0)
                .neutralReviewCount(0)
                .negativeReviewCount(0)
                .commonKeywordsPlaceholder("")
                .summaryContext("No review analytics available yet.")
                .summaryReady(false)
                .lastAggregatedAt(null)
                .latestReviewSubmittedAt(null)
                .build();
    }

    private Map<Integer, Integer> initializeDistribution() {
        Map<Integer, Integer> distribution = new HashMap<>();
        distribution.put(1, 0);
        distribution.put(2, 0);
        distribution.put(3, 0);
        distribution.put(4, 0);
        distribution.put(5, 0);
        return distribution;
    }

    private String buildCommonKeywordsPlaceholder(List<Review> reviews) {
        Map<String, Long> wordCounts = reviews.stream()
                .flatMap(review -> Arrays.stream(
                        ((review.getReviewTitle() == null ? "" : review.getReviewTitle()) + " "
                                + (review.getReviewBody() == null ? "" : review.getReviewBody()))
                                .toLowerCase(Locale.ROOT)
                                .replaceAll("[^a-z0-9 ]", " ")
                                .split("\\s+")
                ))
                .map(String::trim)
                .filter(word -> word.length() >= 4)
                .filter(word -> !STOP_WORDS.contains(word))
                .collect(Collectors.groupingBy(word -> word, Collectors.counting()));

        return wordCounts.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining(", "));
    }

    private String buildSummaryContext(
            Product product,
            int totalReviews,
            BigDecimal averageRating,
            String commonKeywords,
            int positiveReviewCount,
            int negativeReviewCount
    ) {
        return "Product=%s | totalReviews=%d | averageRating=%s | positive=%d | negative=%d | commonKeywords=%s"
                .formatted(
                        product.getName(),
                        totalReviews,
                        averageRating == null ? "N/A" : averageRating,
                        positiveReviewCount,
                        negativeReviewCount,
                        commonKeywords == null || commonKeywords.isBlank() ? "none" : commonKeywords
                );
    }
}
