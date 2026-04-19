package com.fitcart.api.review.mapper;

import com.fitcart.api.review.domain.entity.Review;
import com.fitcart.api.review.domain.entity.ReviewAnalytics;
import com.fitcart.api.review.dto.ReviewAnalyticsResponse;
import com.fitcart.api.review.dto.ReviewResponse;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getProduct().getId(),
                review.getProduct().getName(),
                review.getReviewerName(),
                review.getRating(),
                review.getReviewTitle(),
                review.getReviewBody(),
                review.isVerifiedPurchase(),
                review.getSentimentLabel(),
                review.getSubmittedAt()
        );
    }

    public ReviewAnalyticsResponse toAnalyticsResponse(ReviewAnalytics analytics) {
        Map<Integer, Integer> distribution = new LinkedHashMap<>();
        distribution.put(5, analytics.getFiveStarCount());
        distribution.put(4, analytics.getFourStarCount());
        distribution.put(3, analytics.getThreeStarCount());
        distribution.put(2, analytics.getTwoStarCount());
        distribution.put(1, analytics.getOneStarCount());

        return new ReviewAnalyticsResponse(
                analytics.getProduct().getId(),
                analytics.getProduct().getName(),
                analytics.getTotalReviews(),
                analytics.getAverageRating(),
                distribution,
                analytics.getVerifiedPurchaseCount(),
                analytics.getPositiveReviewCount(),
                analytics.getNeutralReviewCount(),
                analytics.getNegativeReviewCount(),
                analytics.getLatestReviewSubmittedAt(),
                analytics.getCommonKeywordsPlaceholder(),
                analytics.getSummaryContext(),
                analytics.isSummaryReady(),
                analytics.getLastAggregatedAt()
        );
    }
}
