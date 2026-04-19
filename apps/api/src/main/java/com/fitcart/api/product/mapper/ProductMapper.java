package com.fitcart.api.product.mapper;

import com.fitcart.api.product.domain.entity.Product;
import com.fitcart.api.product.dto.ProductDetailResponse;
import com.fitcart.api.product.dto.ProductSummaryResponse;
import com.fitcart.api.review.dto.ReviewResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class ProductMapper {

    public ProductSummaryResponse toSummary(Product product) {
        return new ProductSummaryResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getCategory().getName(),
                product.getBrand().getName(),
                product.getPrice(),
                product.getCurrencyCode(),
                product.getShortDescription(),
                product.getProteinGrams(),
                product.getSugarGrams(),
                product.getRatingAverage(),
                product.getDietaryFlags().stream()
                        .map(flag -> flag.getName())
                        .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new))
        );
    }

    public ProductDetailResponse toDetail(Product product, List<ReviewResponse> reviews) {
        return new ProductDetailResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getShortDescription(),
                product.getPrice(),
                product.getCurrencyCode(),
                product.getProteinGrams(),
                product.getSugarGrams(),
                product.getRatingAverage(),
                product.isActive(),
                new ProductDetailResponse.ReferenceItem(product.getBrand().getId(), product.getBrand().getName()),
                new ProductDetailResponse.ReferenceItem(product.getCategory().getId(), product.getCategory().getName()),
                toReferenceItems(product.getGoals()),
                toReferenceItems(product.getDietaryFlags()),
                reviews
        );
    }

    private Set<ProductDetailResponse.ReferenceItem> toReferenceItems(Set<?> entities) {
        return entities.stream()
                .map(entity -> {
                    if (entity instanceof com.fitcart.api.goal.domain.entity.Goal goal) {
                        return new ProductDetailResponse.ReferenceItem(goal.getId(), goal.getName());
                    }
                    if (entity instanceof com.fitcart.api.dietaryflag.domain.entity.DietaryFlag flag) {
                        return new ProductDetailResponse.ReferenceItem(flag.getId(), flag.getName());
                    }
                    throw new IllegalArgumentException("Unsupported reference entity type");
                })
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }
}
