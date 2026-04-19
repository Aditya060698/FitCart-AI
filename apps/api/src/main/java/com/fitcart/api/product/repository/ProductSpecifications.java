package com.fitcart.api.product.repository;

import com.fitcart.api.product.domain.entity.Product;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Set;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, criteriaBuilder) ->
                categoryId == null ? null : criteriaBuilder.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> hasBrand(Long brandId) {
        return (root, query, criteriaBuilder) ->
                brandId == null ? null : criteriaBuilder.equal(root.get("brand").get("id"), brandId);
    }

    public static Specification<Product> hasActive(Boolean active) {
        return (root, query, criteriaBuilder) ->
                active == null ? null : criteriaBuilder.equal(root.get("active"), active);
    }

    public static Specification<Product> matchesSearch(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return null;
            }

            String searchPattern = "%" + search.trim().toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("slug")), searchPattern)
            );
        };
    }

    public static Specification<Product> priceGreaterThanOrEqualTo(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) ->
                minPrice == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> priceLessThanOrEqualTo(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) ->
                maxPrice == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Product> proteinGreaterThanOrEqualTo(BigDecimal minProtein) {
        return (root, query, criteriaBuilder) ->
                minProtein == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("proteinGrams"), minProtein);
    }

    public static Specification<Product> proteinLessThanOrEqualTo(BigDecimal maxProtein) {
        return (root, query, criteriaBuilder) ->
                maxProtein == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("proteinGrams"), maxProtein);
    }

    public static Specification<Product> sugarGreaterThanOrEqualTo(BigDecimal minSugar) {
        return (root, query, criteriaBuilder) ->
                minSugar == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("sugarGrams"), minSugar);
    }

    public static Specification<Product> sugarLessThanOrEqualTo(BigDecimal maxSugar) {
        return (root, query, criteriaBuilder) ->
                maxSugar == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("sugarGrams"), maxSugar);
    }

    public static Specification<Product> ratingGreaterThanOrEqualTo(BigDecimal minRating) {
        return (root, query, criteriaBuilder) ->
                minRating == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("ratingAverage"), minRating);
    }

    public static Specification<Product> hasGoal(Long goalId) {
        return (root, query, criteriaBuilder) -> {
            if (goalId == null) {
                return null;
            }

            query.distinct(true);
            return criteriaBuilder.equal(root.join("goals", JoinType.LEFT).get("id"), goalId);
        };
    }

    public static Specification<Product> hasDietaryFlags(Set<Long> dietaryFlagIds) {
        return (root, query, criteriaBuilder) -> {
            if (dietaryFlagIds == null || dietaryFlagIds.isEmpty()) {
                return null;
            }

            query.distinct(true);
            return root.join("dietaryFlags", JoinType.LEFT).get("id").in(dietaryFlagIds);
        };
    }
}
