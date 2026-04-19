package com.fitcart.api.product.service.impl;

import com.fitcart.api.brand.domain.entity.Brand;
import com.fitcart.api.brand.repository.BrandRepository;
import com.fitcart.api.autocomplete.service.AutocompleteService;
import com.fitcart.api.category.domain.entity.Category;
import com.fitcart.api.category.repository.CategoryRepository;
import com.fitcart.api.common.cache.CacheNames;
import com.fitcart.api.common.pagination.PageResponse;
import com.fitcart.api.dietaryflag.domain.entity.DietaryFlag;
import com.fitcart.api.dietaryflag.repository.DietaryFlagRepository;
import com.fitcart.api.goal.domain.entity.Goal;
import com.fitcart.api.goal.repository.GoalRepository;
import com.fitcart.api.product.domain.entity.Product;
import com.fitcart.api.product.dto.CreateProductRequest;
import com.fitcart.api.product.dto.ProductDetailResponse;
import com.fitcart.api.product.dto.ProductFilterRequest;
import com.fitcart.api.product.dto.ProductSummaryResponse;
import com.fitcart.api.product.mapper.ProductMapper;
import com.fitcart.api.product.repository.ProductRepository;
import com.fitcart.api.product.repository.ProductSpecifications;
import com.fitcart.api.product.service.ProductService;
import com.fitcart.api.review.dto.ReviewResponse;
import com.fitcart.api.review.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final GoalRepository goalRepository;
    private final DietaryFlagRepository dietaryFlagRepository;
    private final ReviewRepository reviewRepository;
    private final ProductMapper productMapper;
    private final AutocompleteService autocompleteService;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.ADVISOR_TOP_PRODUCTS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.AUTOCOMPLETE_RESULTS, allEntries = true)
    })
    public ProductDetailResponse createProduct(CreateProductRequest request) {
        Brand brand = brandRepository.findById(request.brandId())
                .orElseThrow(() -> new EntityNotFoundException("Brand not found for id " + request.brandId()));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found for id " + request.categoryId()));

        Product product = Product.builder()
                .brand(brand)
                .category(category)
                .sku(request.sku())
                .name(request.name())
                .slug(request.slug())
                .shortDescription(request.shortDescription())
                .description(request.description())
                .price(request.price())
                .currencyCode(request.currencyCode())
                .proteinGrams(request.proteinGrams())
                .sugarGrams(request.sugarGrams())
                .ratingAverage(request.ratingAverage())
                .active(true)
                .goals(resolveGoals(request.goalIds()))
                .dietaryFlags(resolveDietaryFlags(request.dietaryFlagIds()))
                .build();

        Product saved = productRepository.save(product);
        autocompleteService.rebuildIndex();
        return productMapper.toDetail(saved, List.of());
    }

    @Override
    public PageResponse<ProductSummaryResponse> getProducts(
            ProductFilterRequest filter,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sortBy, sortDir));

        Specification<Product> specification = Specification
                .where(ProductSpecifications.hasCategory(filter.categoryId()))
                .and(ProductSpecifications.hasBrand(filter.brandId()))
                .and(ProductSpecifications.hasActive(filter.active()))
                .and(ProductSpecifications.matchesSearch(filter.search()))
                .and(ProductSpecifications.priceGreaterThanOrEqualTo(filter.minPrice()))
                .and(ProductSpecifications.priceLessThanOrEqualTo(filter.maxPrice()))
                .and(ProductSpecifications.hasGoal(filter.goalId()))
                .and(ProductSpecifications.proteinGreaterThanOrEqualTo(filter.minProteinGrams()))
                .and(ProductSpecifications.proteinLessThanOrEqualTo(filter.maxProteinGrams()))
                .and(ProductSpecifications.sugarGreaterThanOrEqualTo(filter.minSugarGrams()))
                .and(ProductSpecifications.sugarLessThanOrEqualTo(filter.maxSugarGrams()))
                .and(ProductSpecifications.ratingGreaterThanOrEqualTo(filter.minRating()))
                .and(ProductSpecifications.hasDietaryFlags(filter.dietaryFlagIds()));

        return PageResponse.from(
                productRepository.findAll(specification, pageable)
                        .map(productMapper::toSummary)
        );
    }

    @Override
    public ProductDetailResponse getProduct(Long id) {
        Product product = productRepository.findDetailById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found for id " + id));

        List<ReviewResponse> reviews = reviewRepository.findByProductIdOrderBySubmittedAtDesc(id)
                .stream()
                .limit(10)
                .map(review -> new ReviewResponse(
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
                ))
                .toList();

        return productMapper.toDetail(product, reviews);
    }

    private Sort buildSort(String sortBy, String sortDir) {
        String resolvedSortBy = switch (sortBy == null ? "newest" : sortBy) {
            case "price" -> "price";
            case "name" -> "name";
            case "rating" -> "ratingAverage";
            case "protein" -> "proteinGrams";
            case "sugar" -> "sugarGrams";
            default -> "createdAt";
        };

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        if ("name".equals(resolvedSortBy)) {
            direction = Sort.Direction.ASC;
        }

        return Sort.by(direction, resolvedSortBy);
    }

    private Set<Goal> resolveGoals(Set<Long> goalIds) {
        if (goalIds == null || goalIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        List<Goal> goals = goalRepository.findAllById(goalIds);
        return new LinkedHashSet<>(goals);
    }

    private Set<DietaryFlag> resolveDietaryFlags(Set<Long> dietaryFlagIds) {
        if (dietaryFlagIds == null || dietaryFlagIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        List<DietaryFlag> dietaryFlags = dietaryFlagRepository.findAllById(dietaryFlagIds);
        return new LinkedHashSet<>(dietaryFlags);
    }
}
