package com.fitcart.api.advisor.service.impl;

import com.fitcart.api.advisor.dto.AdvisorQueryRequest;
import com.fitcart.api.advisor.dto.AdvisorResponse;
import com.fitcart.api.advisor.dto.ParsedAdvisorIntentResponse;
import com.fitcart.api.advisor.service.AdvisorQueryParser;
import com.fitcart.api.common.pagination.PageResponse;
import com.fitcart.api.personalization.service.PersonalizationService;
import com.fitcart.api.product.dto.ProductSummaryResponse;
import com.fitcart.api.product.service.ProductService;
import com.fitcart.api.review.dto.ReviewAnalyticsResponse;
import com.fitcart.api.review.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdvisorServiceImplTest {

    @Mock
    private AdvisorQueryParser advisorQueryParser;

    @Mock
    private CachedAdvisorRecommendationService cachedAdvisorRecommendationService;

    @Mock
    private PersonalizationService personalizationService;

    @Mock
    private ProductService productService;

    @Mock
    private ReviewService reviewService;

    private AdvisorServiceImpl advisorService;

    @BeforeEach
    void setUp() {
        advisorService = new AdvisorServiceImpl(
                cachedAdvisorRecommendationService,
                advisorQueryParser,
                personalizationService,
                productService,
                reviewService
        );
    }

    @Test
    void adviseShouldReturnStructuredAndNaturalLanguageRecommendation() {
        ParsedAdvisorIntentResponse parsedIntent = new ParsedAdvisorIntentResponse(
                "best whey under 2500 for muscle gain with low sugar",
                "whey",
                "muscle gain",
                new BigDecimal("2500"),
                new BigDecimal("4"),
                Set.of("whey", "muscle gain", "low sugar"),
                Set.of()
        );

        when(advisorQueryParser.parse(any())).thenReturn(parsedIntent);
        when(cachedAdvisorRecommendationService.recommend(any())).thenReturn(new AdvisorResponse(
                parsedIntent,
                List.of(),
                "Lean Whey Isolate is a grounded recommendation.",
                false,
                null,
                null
        ));

        AdvisorResponse response = advisorService.advise(new AdvisorQueryRequest(
                "Best whey under 2500 for muscle gain with low sugar",
                3,
                "demo-user"
        ));

        assertThat(response.parsedIntent().goal()).isEqualTo("muscle gain");
        assertThat(response.answer()).contains("Lean Whey Isolate");
        assertThat(response.answer()).contains("grounded");
        assertThat(response.degraded()).isFalse();
    }

    @Test
    void adviseShouldFallbackToCatalogResultsWhenRecommendationLayerFails() {
        ParsedAdvisorIntentResponse parsedIntent = new ParsedAdvisorIntentResponse(
                "best whey under 2500",
                "whey",
                "muscle gain",
                new BigDecimal("2500"),
                null,
                Set.of("whey"),
                Set.of()
        );

        when(advisorQueryParser.parse(any())).thenReturn(parsedIntent);
        doThrow(new IllegalStateException("recommendation pipeline failed"))
                .when(cachedAdvisorRecommendationService).recommend(any());
        when(productService.getProducts(any(), any(Integer.class), any(Integer.class), any(), any())).thenReturn(
                new PageResponse<>(
                        List.of(new ProductSummaryResponse(
                                7L,
                                "Fallback Whey",
                                "fallback-whey",
                                "Protein Powder",
                                "FitCart",
                                new BigDecimal("1999"),
                                "INR",
                                "Structured catalog fallback",
                                new BigDecimal("24"),
                                new BigDecimal("3"),
                                new BigDecimal("4.4"),
                                Set.of("High Protein")
                        )),
                        0,
                        3,
                        1,
                        1,
                        true,
                        true
                )
        );
        when(reviewService.getReviewAnalyticsByProductIds(List.of(7L))).thenReturn(Map.of(
                7L,
                new ReviewAnalyticsResponse(
                        7L,
                        "Fallback Whey",
                        20,
                        new BigDecimal("4.4"),
                        Map.of(),
                        14,
                        12,
                        4,
                        4,
                        null,
                        "taste, recovery",
                        "Review analytics available from structured data.",
                        true,
                        null
                )
        ));

        AdvisorResponse response = advisorService.advise(new AdvisorQueryRequest(
                "Best whey under 2500",
                3,
                "demo-user"
        ));

        assertThat(response.degraded()).isTrue();
        assertThat(response.fallbackMode()).isEqualTo("CATALOG_SEARCH");
        assertThat(response.notice()).contains("comparison still work");
        assertThat(response.recommendations()).hasSize(1);
        assertThat(response.recommendations().get(0).name()).isEqualTo("Fallback Whey");
    }
}
