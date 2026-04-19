package com.fitcart.api.personalization.service.impl;

import com.fitcart.api.brand.domain.entity.Brand;
import com.fitcart.api.category.domain.entity.Category;
import com.fitcart.api.personalization.domain.entity.SavedProduct;
import com.fitcart.api.personalization.domain.entity.SearchHistoryEntry;
import com.fitcart.api.personalization.domain.entity.UserPreferenceProfile;
import com.fitcart.api.personalization.dto.PersonalizationContext;
import com.fitcart.api.personalization.repository.SavedProductRepository;
import com.fitcart.api.personalization.repository.SearchHistoryEntryRepository;
import com.fitcart.api.personalization.repository.UserPreferenceProfileRepository;
import com.fitcart.api.product.domain.entity.Product;
import com.fitcart.api.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalizationServiceImplTest {

    @Mock
    private UserPreferenceProfileRepository profileRepository;

    @Mock
    private SavedProductRepository savedProductRepository;

    @Mock
    private SearchHistoryEntryRepository searchHistoryEntryRepository;

    @Mock
    private ProductRepository productRepository;

    private PersonalizationServiceImpl personalizationService;

    @BeforeEach
    void setUp() {
        personalizationService = new PersonalizationServiceImpl(
                profileRepository,
                savedProductRepository,
                searchHistoryEntryRepository,
                productRepository
        );
    }

    @Test
    void getContextShouldCombineProfileSavedProductsAndSearchHistory() {
        when(profileRepository.findByUserReference("demo-user")).thenReturn(Optional.of(
                UserPreferenceProfile.builder()
                        .userReference("demo-user")
                        .minBudget(new BigDecimal("1000"))
                        .maxBudget(new BigDecimal("2500"))
                        .primaryGoal("muscle gain")
                        .dietaryPreferences("high protein,low sugar")
                        .build()
        ));
        when(savedProductRepository.findByUserReferenceOrderByCreatedAtDesc("demo-user")).thenReturn(List.of(
                SavedProduct.builder()
                        .userReference("demo-user")
                        .product(Product.builder()
                                .id(1L)
                                .name("Lean Whey Isolate")
                                .brand(Brand.builder().name("FitCart").build())
                                .category(Category.builder().name("Protein Powder").build())
                                .build())
                        .build()
        ));
        when(searchHistoryEntryRepository.findTop20ByUserReferenceOrderBySearchedAtDesc("demo-user")).thenReturn(List.of(
                SearchHistoryEntry.builder()
                        .userReference("demo-user")
                        .queryText("low sugar whey for muscle gain")
                        .searchedAt(OffsetDateTime.now())
                        .build()
        ));

        PersonalizationContext context = personalizationService.getContext("demo-user");

        assertThat(context.maxBudget()).isEqualByComparingTo("2500");
        assertThat(context.preferredBrands()).contains("fitcart");
        assertThat(context.preferredCategories()).contains("protein powder");
        assertThat(context.dietaryPreferences()).contains("high protein", "low sugar");
        assertThat(context.savedProductIds()).contains(1L);
        assertThat(context.recentQueryTopics()).contains("sugar", "whey", "muscle", "gain");
    }
}
