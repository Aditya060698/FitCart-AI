package com.fitcart.api.review.controller;

import com.fitcart.api.common.api.ApiResponse;
import com.fitcart.api.review.dto.CreateReviewRequest;
import com.fitcart.api.review.dto.ReviewAnalyticsResponse;
import com.fitcart.api.review.dto.ReviewResponse;
import com.fitcart.api.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product review endpoints")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a review")
    public ApiResponse<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request) {
        return ApiResponse.success(reviewService.createReview(request));
    }

    @GetMapping("/products/{productId}")
    @Operation(summary = "Get reviews for a product")
    public ApiResponse<List<ReviewResponse>> getReviewsByProduct(@PathVariable Long productId) {
        return ApiResponse.success(reviewService.getReviewsByProduct(productId));
    }

    @GetMapping("/products/{productId}/analytics")
    @Operation(summary = "Get review analytics for a product")
    public ApiResponse<ReviewAnalyticsResponse> getReviewAnalyticsByProduct(@PathVariable Long productId) {
        return ApiResponse.success(reviewService.getReviewAnalyticsByProduct(productId));
    }
}
