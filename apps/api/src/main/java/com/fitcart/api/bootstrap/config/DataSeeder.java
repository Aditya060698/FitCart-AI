package com.fitcart.api.bootstrap.config;

import com.fitcart.api.brand.domain.entity.Brand;
import com.fitcart.api.brand.repository.BrandRepository;
import com.fitcart.api.category.domain.entity.Category;
import com.fitcart.api.category.repository.CategoryRepository;
import com.fitcart.api.dietaryflag.domain.entity.DietaryFlag;
import com.fitcart.api.dietaryflag.repository.DietaryFlagRepository;
import com.fitcart.api.goal.domain.entity.Goal;
import com.fitcart.api.goal.repository.GoalRepository;
import com.fitcart.api.product.domain.entity.Product;
import com.fitcart.api.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final GoalRepository goalRepository;
    private final DietaryFlagRepository dietaryFlagRepository;
    private final ProductRepository productRepository;

    @Bean
    CommandLineRunner seedData() {
        return args -> {
            if (brandRepository.count() > 0 || categoryRepository.count() > 0 || productRepository.count() > 0) {
                return;
            }

            Brand brand = brandRepository.save(
                    Brand.builder()
                            .name("FitCart Labs")
                            .websiteUrl("https://fitcart.example")
                            .countryCode("US")
                            .build()
            );

            Category category = categoryRepository.save(
                    Category.builder()
                            .slug("protein")
                            .name("Protein")
                            .description("Protein supplements and recovery products.")
                            .build()
            );

            Goal muscleGain = goalRepository.save(
                    Goal.builder()
                            .slug("muscle-gain")
                            .name("Muscle Gain")
                            .description("Products suitable for muscle gain programs.")
                            .build()
            );

            Goal recovery = goalRepository.save(
                    Goal.builder()
                            .slug("recovery")
                            .name("Recovery")
                            .description("Products positioned for recovery and post-workout support.")
                            .build()
            );

            DietaryFlag glutenFree = dietaryFlagRepository.save(
                    DietaryFlag.builder()
                            .slug("gluten-free")
                            .name("Gluten Free")
                            .description("Suitable for users avoiding gluten.")
                            .build()
            );

            DietaryFlag lowSugar = dietaryFlagRepository.save(
                    DietaryFlag.builder()
                            .slug("low-sugar")
                            .name("Low Sugar")
                            .description("Products with reduced sugar content.")
                            .build()
            );

            productRepository.save(
                    Product.builder()
                            .brand(brand)
                            .category(category)
                            .sku("FIT-WHEY-CORE")
                            .name("FitCart Whey Core")
                            .slug("fitcart-whey-core")
                            .shortDescription("Starter whey protein product for backend scaffolding.")
                            .description("A starter product used to validate listing and detail flows.")
                            .price(new BigDecimal("39.99"))
                            .currencyCode("USD")
                            .proteinGrams(new BigDecimal("24.00"))
                            .sugarGrams(new BigDecimal("2.00"))
                            .ratingAverage(new BigDecimal("4.40"))
                            .active(true)
                            .goals(new LinkedHashSet<>(Set.of(muscleGain, recovery)))
                            .dietaryFlags(new LinkedHashSet<>(Set.of(glutenFree, lowSugar)))
                            .build()
            );
        };
    }
}
