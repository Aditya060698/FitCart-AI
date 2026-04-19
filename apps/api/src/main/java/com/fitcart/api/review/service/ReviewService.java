package com.fitcart.api.review.service;

import com.fitcart.api.review.dto.CreateReviewRequest;
import com.fitcart.api.review.dto.ReviewAnalyticsResponse;
import com.fitcart.api.review.dto.ReviewResponse;

import java.util.List;
import java.util.Map;

public interface ReviewService {

    ReviewResponse createReview(CreateReviewRequest request);

    List<ReviewResponse> getReviewsByProduct(Long productId);

    ReviewAnalyticsResponse getReviewAnalyticsByProduct(Long productId);

    Map<Long, ReviewAnalyticsResponse> getReviewAnalyticsByProductIds(List<Long> productIds);
}
