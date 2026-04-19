package com.fitcart.api.review.repository;

import com.fitcart.api.review.domain.entity.ReviewAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewAnalyticsRepository extends JpaRepository<ReviewAnalytics, Long> {

    Optional<ReviewAnalytics> findByProductId(Long productId);

    List<ReviewAnalytics> findAllByProductIdIn(Collection<Long> productIds);
}
