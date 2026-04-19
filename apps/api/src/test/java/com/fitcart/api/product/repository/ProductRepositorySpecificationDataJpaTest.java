package com.fitcart.api.product.repository;

import com.fitcart.api.brand.domain.entity.Brand;
import com.fitcart.api.brand.repository.BrandRepository;
import com.fitcart.api.category.domain.entity.Category;
import com.fitcart.api.category.repository.CategoryRepository;
import com.fitcart.api.dietaryflag.domain.entity.DietaryFlag;
import com.fitcart.api.dietaryflag.repository.DietaryFlagRepository;
import com.fitcart.api.goal.domain.entity.Goal;
import com.fitcart.api.goal.repository.GoalRepository;
import com.fitcart.api.product.domain.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositorySpecificationDataJpaTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private DietaryFlagRepository dietaryFlagRepository;

    private Goal muscleGainGoal;
    private DietaryFlag highProteinFlag;

    @BeforeEach
    void setUp() {
        Brand fitCart = brandRepository.save(Brand.builder()
                .name("FitCart")
                .websiteUrl("https://fitcart.test")
                .countryCode("IN")
                .build());

        Brand bulkLabs = brandRepository.save(Brand.builder()
                .name("Bulk Labs")
                .websiteUrl("https://bulklabs.test")
                .countryCode("IN")
                .build());

        Category proteinPowder = categoryRepository.save(Category.builder()
                .name("Protein Powder")
                .slug("protein-powder")
                .description("Protein products")
                .build());

        muscleGainGoal = goalRepository.save(Goal.builder()
                .name("Muscle Gain")
                .slug("muscle-gain")
                .description("Mass and strength support")
                .build());

        Goal weightLossGoal = goalRepository.save(Goal.builder()
                .name("Weight Loss")
                .slug("weight-loss")
                .description("Cutting support")
                .build());

        highProteinFlag = dietaryFlagRepository.save(DietaryFlag.builder()
                .name("High Protein")
                .slug("high-protein")
                .description("Protein-forward products")
                .build());

        DietaryFlag lowSugarFlag = dietaryFlagRepository.save(DietaryFlag.builder()
                .name("Low Sugar")
                .slug("low-sugar")
                .description("Low sugar products")
                .build());

        productRepository.save(Product.builder()
                .brand(fitCart)
                .category(proteinPowder)
                .name("Lean Whey Isolate")
                .slug("lean-whey-isolate")
                .shortDescription("Low sugar whey")
                .description("Whey isolate for cutting")
                .price(new BigDecimal("1999.00"))
                .currencyCode("INR")
                .proteinGrams(new BigDecimal("26.00"))
                .sugarGrams(new BigDecimal("2.00"))
                .ratingAverage(new BigDecimal("4.60"))
                .active(true)
                .goals(Set.of(muscleGainGoal, weightLossGoal))
                .dietaryFlags(Set.of(highProteinFlag, lowSugarFlag))
                .build());

        productRepository.save(Product.builder()
                .brand(bulkLabs)
                .category(proteinPowder)
                .name("Mass Builder Shake")
                .slug("mass-builder-shake")
                .shortDescription("Higher sugar gainer")
                .description("Mass gainer for bulking")
                .price(new BigDecimal("2499.00"))
                .currencyCode("INR")
                .proteinGrams(new BigDecimal("20.00"))
                .sugarGrams(new BigDecimal("9.00"))
                .ratingAverage(new BigDecimal("4.10"))
                .active(true)
                .goals(Set.of(muscleGainGoal))
                .dietaryFlags(Set.of(highProteinFlag))
                .build());
    }

    @Test
    void shouldFilterProductsByGoalDietaryFlagAndNumericRanges() {
        Specification<Product> specification = Specification
                .where(ProductSpecifications.hasGoal(muscleGainGoal.getId()))
                .and(ProductSpecifications.hasDietaryFlags(Set.of(highProteinFlag.getId())))
                .and(ProductSpecifications.priceLessThanOrEqualTo(new BigDecimal("2100.00")))
                .and(ProductSpecifications.sugarLessThanOrEqualTo(new BigDecimal("3.00")))
                .and(ProductSpecifications.ratingGreaterThanOrEqualTo(new BigDecimal("4.50")));

        Page<Product> page = productRepository.findAll(specification, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getSlug()).isEqualTo("lean-whey-isolate");
    }

    @Test
    void shouldMatchSearchAcrossNameAndSlug() {
        Specification<Product> specification = Specification
                .where(ProductSpecifications.matchesSearch("mass-builder"))
                .and(ProductSpecifications.hasActive(true));

        Page<Product> page = productRepository.findAll(specification, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Mass Builder Shake");
    }
}
