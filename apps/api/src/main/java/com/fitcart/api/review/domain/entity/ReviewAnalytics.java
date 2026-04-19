package com.fitcart.api.review.domain.entity;

import com.fitcart.api.common.domain.BaseAuditableEntity;
import com.fitcart.api.product.domain.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "review_analytics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAnalytics extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(nullable = false)
    private Integer totalReviews;

    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(nullable = false)
    private Integer oneStarCount;

    @Column(nullable = false)
    private Integer twoStarCount;

    @Column(nullable = false)
    private Integer threeStarCount;

    @Column(nullable = false)
    private Integer fourStarCount;

    @Column(nullable = false)
    private Integer fiveStarCount;

    @Column(nullable = false)
    private Integer verifiedPurchaseCount;

    @Column(nullable = false)
    private Integer positiveReviewCount;

    @Column(nullable = false)
    private Integer neutralReviewCount;

    @Column(nullable = false)
    private Integer negativeReviewCount;

    private OffsetDateTime latestReviewSubmittedAt;

    @Column(length = 1000)
    private String commonKeywordsPlaceholder;

    @Column(columnDefinition = "TEXT")
    private String summaryContext;

    @Column(nullable = false)
    private boolean summaryReady;

    private OffsetDateTime lastAggregatedAt;
}
