package com.fitcart.api.ranking.service.impl;

import com.fitcart.api.brand.domain.entity.Brand;
import com.fitcart.api.category.domain.entity.Category;
import com.fitcart.api.dietaryflag.domain.entity.DietaryFlag;
import com.fitcart.api.goal.domain.entity.Goal;
import com.fitcart.api.product.domain.entity.Product;
import com.fitcart.api.product.repository.ProductRepository;
import com.fitcart.api.ranking.dto.RankProductsRequest;
import com.fitcart.api.ranking.dto.RankProductsResponse;
import com.fitcart.api.ranking.dto.RankingCandidateRequest;
import com.fitcart.api.ranking.dto.RankingUserConstraints;
import com.fitcart.api.review.domain.entity.ReviewAnalytics;
import com.fitcart.api.review.repository.ReviewAnalyticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ReviewAnalyticsRepository reviewAnalyticsRepository;

    @Mock
    private com.fitcart.api.personalization.service.PersonalizationService personalizationService;

    private RankingServiceImpl rankingService;

    @BeforeEach
    void setUp() {
        rankingService = new RankingServiceImpl(productRepository, reviewAnalyticsRepository, personalizationService);
    }

    @Test
    void rankProductsShouldPrioritizeLowSugarBudgetFriendlyProtein() {
        Product leanProtein = buildProduct(
                1L,
                "Lean Whey Isolate",
                "lean-whey-isolate",
                "Protein Powder",
                "FitCart",
                new BigDecimal("34.99"),
                new BigDecimal("26.00"),
                new BigDecimal("2.00"),
                new BigDecimal("4.60"),
                Set.of("High Protein"),
                Set.of("Cutting")
        );
        Product sugaryProtein = buildProduct(
                2L,
                "Mass Builder Shake",
                "mass-builder-shake",
                "Protein Powder",
                "Bulk Labs",
                new BigDecimal("44.99"),
                new BigDecimal("25.00"),
                new BigDecimal("10.00"),
                new BigDecimal("4.20"),
                Set.of("High Protein"),
                Set.of("Muscle Gain")
        );

        when(productRepository.findAllById(anyCollection())).thenReturn(List.of(leanProtein, sugaryProtein));
        when(reviewAnalyticsRepository.findAllByProductIdIn(anyCollection())).thenReturn(List.of(
                buildAnalytics(leanProtein, 120, 90, 12, new BigDecimal("4.60")),
                buildAnalytics(sugaryProtein, 80, 45, 20, new BigDecimal("4.20"))
        ));
        when(personalizationService.getContext("demo-user")).thenReturn(
                new com.fitcart.api.personalization.dto.PersonalizationContext(
                        null, null, Set.of(), Set.of(), Set.of(), Set.of(), Set.of()
                )
        );

        RankProductsResponse response = rankingService.rankProducts(new RankProductsRequest(
                List.of(
                        new RankingCandidateRequest(1L, 0.91, 0.70),
                        new RankingCandidateRequest(2L, 0.82, 0.68)
                ),
                new RankingUserConstraints(
                        new BigDecimal("40.00"),
                        "cutting",
                        Set.of("FitCart"),
                        Set.of("High Protein"),
                        "demo-user"
                ),
                2
        ));

        assertThat(response.products()).hasSize(2);
        assertThat(response.products().get(0).productId()).isEqualTo(1L);
        assertThat(response.products().get(0).finalScore()).isGreaterThan(response.products().get(1).finalScore());
    }

    @Test
    void rankProductsShouldBoostSavedAndPreferredProductsFromPersonalizationContext() {
        Product standardProtein = buildProduct(
                1L,
                "Standard Whey",
                "standard-whey",
                "Protein Powder",
                "Generic Labs",
                new BigDecimal("39.99"),
                new BigDecimal("24.00"),
                new BigDecimal("3.00"),
                new BigDecimal("4.30"),
                Set.of("High Protein"),
                Set.of("Muscle Gain")
        );
        Product preferredProtein = buildProduct(
                2L,
                "Preferred Whey",
                "preferred-whey",
                "Protein Powder",
                "FitCart",
                new BigDecimal("40.99"),
                new BigDecimal("24.00"),
                new BigDecimal("3.00"),
                new BigDecimal("4.30"),
                Set.of("High Protein"),
                Set.of("Muscle Gain")
        );

        when(personalizationService.getContext("personalized-user")).thenReturn(
                new com.fitcart.api.personalization.dto.PersonalizationContext(
                        null,
                        new BigDecimal("45.00"),
                        Set.of("FitCart"),
                        Set.of("Protein Powder"),
                        Set.of("High Protein"),
                        Set.of("whey"),
                        Set.of(2L)
                )
        );
        when(productRepository.findAllById(anyCollection())).thenReturn(List.of(standardProtein, preferredProtein));
        when(reviewAnalyticsRepository.findAllByProductIdIn(anyCollection())).thenReturn(List.of(
                buildAnalytics(standardProtein, 100, 75, 10, new BigDecimal("4.30")),
                buildAnalytics(preferredProtein, 100, 75, 10, new BigDecimal("4.30"))
        ));

        RankProductsResponse response = rankingService.rankProducts(new RankProductsRequest(
                List.of(
                        new RankingCandidateRequest(1L, 0.82, 0.60),
                        new RankingCandidateRequest(2L, 0.78, 0.60)
                ),
                new RankingUserConstraints(
                        null,
                        "muscle gain",
                        Set.of(),
                        Set.of("High Protein"),
                        "personalized-user"
                ),
                2
        ));

        assertThat(response.products()).hasSize(2);
        assertThat(response.products().get(0).productId()).isEqualTo(2L);
    }

    private Product buildProduct(
            Long id,
            String name,
            String slug,
            String categoryName,
            String brandName,
            BigDecimal price,
            BigDecimal protein,
            BigDecimal sugar,
            BigDecimal rating,
            Set<String> dietaryFlags,
            Set<String> goals
    ) {
        Product product = Product.builder()
                .id(id)
                .name(name)
                .slug(slug)
                .price(price)
                .currencyCode("USD")
                .proteinGrams(protein)
                .sugarGrams(sugar)
                .ratingAverage(rating)
                .active(true)
                .brand(Brand.builder().id(id).name(brandName).build())
                .category(Category.builder().id(id).name(categoryName).slug(slug + "-category").build())
                .build();

        product.setDietaryFlags(dietaryFlags.stream()
                .map(flag -> DietaryFlag.builder().name(flag).slug(flag.toLowerCase().replace(" ", "-")).build())
                .collect(java.util.stream.Collectors.toSet()));
        product.setGoals(goals.stream()
                .map(goal -> Goal.builder().name(goal).slug(goal.toLowerCase().replace(" ", "-")).build())
                .collect(java.util.stream.Collectors.toSet()));

        return product;
    }

    private ReviewAnalytics buildAnalytics(
            Product product,
            int totalReviews,
            int positiveReviews,
            int negativeReviews,
            BigDecimal averageRating
    ) {
        return ReviewAnalytics.builder()
                .product(product)
                .totalReviews(totalReviews)
                .averageRating(averageRating)
                .oneStarCount(2)
                .twoStarCount(3)
                .threeStarCount(8)
                .fourStarCount(35)
                .fiveStarCount(72)
                .verifiedPurchaseCount(totalReviews - 5)
                .positiveReviewCount(positiveReviews)
                .neutralReviewCount(Math.max(totalReviews - positiveReviews - negativeReviews, 0))
                .negativeReviewCount(negativeReviews)
                .summaryReady(true)
                .summaryContext("Summary context")
                .build();
    }
}
