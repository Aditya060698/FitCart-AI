package com.fitcart.api.review.repository;

import com.fitcart.api.review.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductIdOrderBySubmittedAtDesc(Long productId);

    List<Review> findByProductId(Long productId);
}
