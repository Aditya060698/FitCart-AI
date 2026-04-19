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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(length = 80)
    private String source;

    @Column(length = 120)
    private String externalReviewId;

    @Column(length = 150)
    private String reviewerName;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 255)
    private String reviewTitle;

    @Column(columnDefinition = "TEXT")
    private String reviewBody;

    @Column(nullable = false)
    private boolean verifiedPurchase;

    @Column(length = 30)
    private String sentimentLabel;

    @Column(nullable = false)
    private OffsetDateTime submittedAt;

    @PrePersist
    protected void onSubmit() {
        if (submittedAt == null) {
            submittedAt = OffsetDateTime.now();
        }
    }
}
