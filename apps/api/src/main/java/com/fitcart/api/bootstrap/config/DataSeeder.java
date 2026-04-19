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
import com.fitcart.api.review.domain.entity.Review;
import com.fitcart.api.review.domain.entity.ReviewAnalytics;
import com.fitcart.api.review.repository.ReviewAnalyticsRepository;
import com.fitcart.api.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private static final List<BrandSeed> BRAND_SEEDS = List.of(
            new BrandSeed("FitCart Labs", "https://fitcart.example", "US"),
            new BrandSeed("Atlas Nutrition", "https://atlasnutrition.example", "US"),
            new BrandSeed("PureForm Sports", "https://pureformsports.example", "CA"),
            new BrandSeed("North Peak Wellness", "https://northpeakwellness.example", "GB"),
            new BrandSeed("CoreFuel Nutrition", "https://corefuel.example", "AU"),
            new BrandSeed("EverActive Health", "https://everactive.example", "DE"),
            new BrandSeed("CleanLift Supplements", "https://cleanlift.example", "US"),
            new BrandSeed("DailyBalance Co.", "https://dailybalance.example", "NL")
    );

    private static final List<ReferenceSeed> CATEGORY_SEEDS = List.of(
            new ReferenceSeed("whey-protein", "Whey Protein", "Fast-digesting protein powders for recovery and lean muscle support."),
            new ReferenceSeed("plant-protein", "Plant Protein", "Vegan-friendly protein blends for daily nutrition and training support."),
            new ReferenceSeed("creatine", "Creatine", "Creatine products positioned for strength, power, and performance support."),
            new ReferenceSeed("pre-workout", "Pre-Workout", "Pre-training energy and focus supplements."),
            new ReferenceSeed("hydration", "Hydration", "Electrolyte and hydration support formulas."),
            new ReferenceSeed("vitamins-minerals", "Vitamins & Minerals", "Micronutrient support for recovery, wellness, and consistency.")
    );

    private static final List<ReferenceSeed> GOAL_SEEDS = List.of(
            new ReferenceSeed("muscle-gain", "Muscle Gain", "Products suitable for users prioritizing lean size and strength gains."),
            new ReferenceSeed("fat-loss", "Fat Loss", "Products aligned with calorie control and cutting phases."),
            new ReferenceSeed("recovery", "Recovery", "Products positioned for post-workout recovery and training readiness."),
            new ReferenceSeed("endurance", "Endurance", "Products that support hydration, sustained training, and conditioning."),
            new ReferenceSeed("strength", "Strength", "Products supporting high-output performance and repeated power efforts."),
            new ReferenceSeed("general-wellness", "General Wellness", "Daily health and nutritional support beyond strict sport use.")
    );

    private static final List<ReferenceSeed> DIETARY_FLAG_SEEDS = List.of(
            new ReferenceSeed("gluten-free", "Gluten Free", "Suitable for users avoiding gluten."),
            new ReferenceSeed("low-sugar", "Low Sugar", "Products with reduced sugar content."),
            new ReferenceSeed("vegan", "Vegan", "Suitable for vegan dietary preferences."),
            new ReferenceSeed("vegetarian", "Vegetarian", "Suitable for vegetarian users."),
            new ReferenceSeed("keto-friendly", "Keto Friendly", "Appropriate for lower-carb diets."),
            new ReferenceSeed("lactose-free", "Lactose Free", "Suitable for users avoiding lactose.")
    );

    private static final List<String> REVIEWER_NAMES = List.of(
            "Aarav", "Maya", "Jordan", "Priya", "Liam", "Sofia", "Arjun", "Nina", "Marcus", "Elena", "Dev", "Riya"
    );

    private static final List<String> POSITIVE_PHRASES = List.of(
            "mixes smoothly and feels easy to digest",
            "fits my budget without feeling low quality",
            "helped me stay consistent during a busy work week",
            "works well after training and tastes better than expected",
            "gives a clean experience without an overly artificial finish"
    );

    private static final List<String> NEUTRAL_PHRASES = List.of(
            "does the job, but the flavor is just average",
            "solid formula overall, though the scoop size is larger than I prefer",
            "useful product, but I still compare it against cheaper options",
            "good enough for daily use, but not the most exciting texture",
            "ingredients look strong, though the packaging could be better"
    );

    private static final List<String> NEGATIVE_PHRASES = List.of(
            "the taste gets repetitive after a few weeks",
            "a bit too sweet for me during a cutting phase",
            "clumps more than I expected unless I shake it hard",
            "price feels high when there are frequent discounts elsewhere",
            "did not stand out enough to become my main pick"
    );

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final GoalRepository goalRepository;
    private final DietaryFlagRepository dietaryFlagRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewAnalyticsRepository reviewAnalyticsRepository;

    @Bean
    CommandLineRunner seedData() {
        return args -> {
            if (productRepository.count() > 0) {
                return;
            }

            Map<String, Brand> brands = seedBrands();
            Map<String, Category> categories = seedCategories();
            Map<String, Goal> goals = seedGoals();
            Map<String, DietaryFlag> dietaryFlags = seedDietaryFlags();

            List<Product> products = buildProducts(brands, categories, goals, dietaryFlags);
            List<Product> savedProducts = productRepository.saveAll(products);

            List<Review> reviews = new ArrayList<>();
            List<ReviewAnalytics> analytics = new ArrayList<>();
            for (int index = 0; index < savedProducts.size(); index++) {
                Product product = savedProducts.get(index);
                List<Review> productReviews = buildReviewsForProduct(product, index);
                reviews.addAll(productReviews);
                analytics.add(buildAnalytics(product, productReviews));
            }

            productRepository.saveAll(savedProducts);
            reviewRepository.saveAll(reviews);
            reviewAnalyticsRepository.saveAll(analytics);
        };
    }

    private Map<String, Brand> seedBrands() {
        Map<String, Brand> brands = new LinkedHashMap<>();
        for (BrandSeed seed : BRAND_SEEDS) {
            Brand brand = brandRepository.save(
                    Brand.builder()
                            .name(seed.name())
                            .websiteUrl(seed.websiteUrl())
                            .countryCode(seed.countryCode())
                            .build()
            );
            brands.put(seed.name(), brand);
        }
        return brands;
    }

    private Map<String, Category> seedCategories() {
        Map<String, Category> categories = new LinkedHashMap<>();
        for (ReferenceSeed seed : CATEGORY_SEEDS) {
            Category category = categoryRepository.save(
                    Category.builder()
                            .slug(seed.slug())
                            .name(seed.name())
                            .description(seed.description())
                            .build()
            );
            categories.put(seed.slug(), category);
        }
        return categories;
    }

    private Map<String, Goal> seedGoals() {
        Map<String, Goal> goals = new LinkedHashMap<>();
        for (ReferenceSeed seed : GOAL_SEEDS) {
            Goal goal = goalRepository.save(
                    Goal.builder()
                            .slug(seed.slug())
                            .name(seed.name())
                            .description(seed.description())
                            .build()
            );
            goals.put(seed.slug(), goal);
        }
        return goals;
    }

    private Map<String, DietaryFlag> seedDietaryFlags() {
        Map<String, DietaryFlag> dietaryFlags = new LinkedHashMap<>();
        for (ReferenceSeed seed : DIETARY_FLAG_SEEDS) {
            DietaryFlag dietaryFlag = dietaryFlagRepository.save(
                    DietaryFlag.builder()
                            .slug(seed.slug())
                            .name(seed.name())
                            .description(seed.description())
                            .build()
            );
            dietaryFlags.put(seed.slug(), dietaryFlag);
        }
        return dietaryFlags;
    }

    private List<Product> buildProducts(
            Map<String, Brand> brands,
            Map<String, Category> categories,
            Map<String, Goal> goals,
            Map<String, DietaryFlag> dietaryFlags
    ) {
        List<ProductSeed> seeds = List.of(
                new ProductSeed("whey-protein", "Whey Isolate", 52.99, 27.0, 1.0, true),
                new ProductSeed("whey-protein", "Whey Concentrate", 39.99, 24.0, 3.0, true),
                new ProductSeed("whey-protein", "Hydro Whey", 61.99, 28.0, 1.0, true),
                new ProductSeed("whey-protein", "Recovery Blend", 47.49, 25.0, 4.0, true),
                new ProductSeed("whey-protein", "Lean Shake", 44.99, 23.0, 2.0, true),
                new ProductSeed("whey-protein", "Night Protein", 49.49, 26.0, 2.0, true),
                new ProductSeed("plant-protein", "Pea Protein", 35.99, 22.0, 2.0, true),
                new ProductSeed("plant-protein", "Vegan Recovery", 41.99, 24.0, 3.0, true),
                new ProductSeed("plant-protein", "Greens Protein", 38.49, 20.0, 2.0, true),
                new ProductSeed("plant-protein", "Complete Plant Blend", 43.99, 25.0, 3.0, true),
                new ProductSeed("plant-protein", "Daily Vegan Protein", 36.99, 21.0, 1.0, true),
                new ProductSeed("plant-protein", "Oat & Pea Fuel", 34.99, 19.0, 2.0, true),
                new ProductSeed("creatine", "Creatine Monohydrate", 19.99, 0.0, 0.0, true),
                new ProductSeed("creatine", "Micronized Creatine", 22.49, 0.0, 0.0, true),
                new ProductSeed("creatine", "Creatine + Electrolytes", 27.99, 0.0, 1.0, true),
                new ProductSeed("creatine", "Daily Strength Stack", 29.99, 1.0, 0.0, true),
                new ProductSeed("creatine", "Performance Creatine", 24.49, 0.0, 0.0, true),
                new ProductSeed("creatine", "Buffered Creatine", 31.99, 0.0, 0.0, true),
                new ProductSeed("pre-workout", "High Stim Pre", 32.99, 0.0, 1.0, true),
                new ProductSeed("pre-workout", "Focus Pre", 29.49, 0.0, 0.0, true),
                new ProductSeed("pre-workout", "Pump Formula", 34.99, 0.0, 0.0, true),
                new ProductSeed("pre-workout", "Low Caffeine Pre", 28.49, 0.0, 2.0, true),
                new ProductSeed("pre-workout", "Endurance Ignite", 30.99, 0.0, 1.0, true),
                new ProductSeed("pre-workout", "Training Spark", 26.99, 0.0, 2.0, true),
                new ProductSeed("hydration", "Electrolyte Tabs", 14.99, 0.0, 1.0, true),
                new ProductSeed("hydration", "Hydration Mix", 18.99, 0.0, 2.0, true),
                new ProductSeed("hydration", "Endurance Hydrate", 21.49, 0.0, 3.0, true),
                new ProductSeed("hydration", "Low Sugar Electrolytes", 17.99, 0.0, 1.0, true),
                new ProductSeed("hydration", "Daily Recovery Salts", 19.49, 0.0, 2.0, true),
                new ProductSeed("hydration", "Heat Session Hydrator", 23.49, 0.0, 3.0, true),
                new ProductSeed("vitamins-minerals", "Magnesium Glycinate", 16.99, 0.0, 0.0, true),
                new ProductSeed("vitamins-minerals", "Omega-3 Softgels", 20.99, 0.0, 0.0, true),
                new ProductSeed("vitamins-minerals", "Vitamin D3 + K2", 18.49, 0.0, 0.0, true),
                new ProductSeed("vitamins-minerals", "Zinc Recovery", 13.99, 0.0, 0.0, true),
                new ProductSeed("vitamins-minerals", "Joint Support Complex", 24.99, 4.0, 0.0, true),
                new ProductSeed("vitamins-minerals", "Sleep Support Magnesium", 17.49, 0.0, 0.0, true)
        );

        List<Product> products = new ArrayList<>();
        List<Brand> brandRotation = new ArrayList<>(brands.values());
        int index = 0;
        for (ProductSeed seed : seeds) {
            Category category = categories.get(seed.categorySlug());
            Brand brand = brandRotation.get(index % brandRotation.size());
            String slug = slugify(brand.getName() + " " + seed.displayName());

            Set<Goal> productGoals = resolveGoals(seed.categorySlug(), index, goals);
            Set<DietaryFlag> productDietaryFlags = resolveDietaryFlags(seed.categorySlug(), seed, index, dietaryFlags);
            BigDecimal ratingAverage = BigDecimal.valueOf(3.8 + ((index % 12) * 0.1)).setScale(2, RoundingMode.HALF_UP);

            products.add(
                    Product.builder()
                            .brand(brand)
                            .category(category)
                            .sku(buildSku(category.getSlug(), brand.getName(), index))
                            .name(brand.getName() + " " + seed.displayName())
                            .slug(slug)
                            .shortDescription(buildShortDescription(seed.categorySlug(), seed.displayName()))
                            .description(buildLongDescription(seed.categorySlug(), seed.displayName(), brand.getName()))
                            .price(BigDecimal.valueOf(seed.basePrice() + ((index % 4) * 2.5)).setScale(2, RoundingMode.HALF_UP))
                            .currencyCode("USD")
                            .proteinGrams(BigDecimal.valueOf(seed.proteinGrams()).setScale(2, RoundingMode.HALF_UP))
                            .sugarGrams(BigDecimal.valueOf(seed.sugarGrams() + (index % 3 == 0 ? 0.0 : 1.0)).setScale(2, RoundingMode.HALF_UP))
                            .ratingAverage(ratingAverage)
                            .active(seed.active())
                            .goals(productGoals)
                            .dietaryFlags(productDietaryFlags)
                            .build()
            );
            index++;
        }

        return products;
    }

    private Set<Goal> resolveGoals(String categorySlug, int index, Map<String, Goal> goals) {
        Set<Goal> resolvedGoals = new LinkedHashSet<>();
        switch (categorySlug) {
            case "whey-protein" -> {
                resolvedGoals.add(goals.get("muscle-gain"));
                resolvedGoals.add(goals.get(index % 2 == 0 ? "recovery" : "strength"));
            }
            case "plant-protein" -> {
                resolvedGoals.add(goals.get("general-wellness"));
                resolvedGoals.add(goals.get(index % 2 == 0 ? "fat-loss" : "recovery"));
            }
            case "creatine" -> {
                resolvedGoals.add(goals.get("strength"));
                resolvedGoals.add(goals.get(index % 2 == 0 ? "muscle-gain" : "recovery"));
            }
            case "pre-workout" -> {
                resolvedGoals.add(goals.get("strength"));
                resolvedGoals.add(goals.get(index % 2 == 0 ? "endurance" : "fat-loss"));
            }
            case "hydration" -> {
                resolvedGoals.add(goals.get("endurance"));
                resolvedGoals.add(goals.get("recovery"));
            }
            case "vitamins-minerals" -> {
                resolvedGoals.add(goals.get("general-wellness"));
                resolvedGoals.add(goals.get(index % 2 == 0 ? "recovery" : "endurance"));
            }
            default -> resolvedGoals.add(goals.get("general-wellness"));
        }
        return resolvedGoals;
    }

    private Set<DietaryFlag> resolveDietaryFlags(
            String categorySlug,
            ProductSeed seed,
            int index,
            Map<String, DietaryFlag> dietaryFlags
    ) {
        Set<DietaryFlag> resolvedFlags = new LinkedHashSet<>();

        if (seed.sugarGrams() <= 2.0) {
            resolvedFlags.add(dietaryFlags.get("low-sugar"));
        }
        if ("plant-protein".equals(categorySlug)) {
            resolvedFlags.add(dietaryFlags.get("vegan"));
            resolvedFlags.add(dietaryFlags.get("vegetarian"));
        }
        if ("whey-protein".equals(categorySlug) && index % 2 == 0) {
            resolvedFlags.add(dietaryFlags.get("lactose-free"));
        }
        if (Set.of("creatine", "pre-workout", "hydration").contains(categorySlug)) {
            resolvedFlags.add(dietaryFlags.get("gluten-free"));
        }
        if (index % 3 == 0) {
            resolvedFlags.add(dietaryFlags.get("keto-friendly"));
        }
        if (!resolvedFlags.contains(dietaryFlags.get("vegan"))) {
            resolvedFlags.add(dietaryFlags.get("vegetarian"));
        }

        return resolvedFlags;
    }

    private List<Review> buildReviewsForProduct(Product product, int productIndex) {
        int reviewCount = 5 + (productIndex % 4);
        List<Review> reviews = new ArrayList<>(reviewCount);

        for (int reviewIndex = 0; reviewIndex < reviewCount; reviewIndex++) {
            int rating = deriveRating(productIndex, reviewIndex);
            String sentiment = rating >= 4 ? "POSITIVE" : rating == 3 ? "NEUTRAL" : "NEGATIVE";
            String reviewerName = REVIEWER_NAMES.get((productIndex + reviewIndex) % REVIEWER_NAMES.size());
            String phrase = sentiment.equals("POSITIVE")
                    ? POSITIVE_PHRASES.get((productIndex + reviewIndex) % POSITIVE_PHRASES.size())
                    : sentiment.equals("NEUTRAL")
                    ? NEUTRAL_PHRASES.get((productIndex + reviewIndex) % NEUTRAL_PHRASES.size())
                    : NEGATIVE_PHRASES.get((productIndex + reviewIndex) % NEGATIVE_PHRASES.size());

            reviews.add(
                    Review.builder()
                            .product(product)
                            .source(reviewIndex % 2 == 0 ? "INTERNAL" : "MARKETPLACE_IMPORT")
                            .externalReviewId(product.getSlug() + "-review-" + reviewIndex)
                            .reviewerName(reviewerName)
                            .rating(rating)
                            .reviewTitle(buildReviewTitle(product.getName(), rating))
                            .reviewBody(buildReviewBody(product.getName(), phrase, rating))
                            .verifiedPurchase(reviewIndex % 3 != 0)
                            .sentimentLabel(sentiment)
                            .submittedAt(OffsetDateTime.now().minusDays((long) productIndex * 2 + reviewIndex + 1))
                            .build()
            );
        }

        return reviews;
    }

    private ReviewAnalytics buildAnalytics(Product product, List<Review> reviews) {
        int oneStarCount = countByRating(reviews, 1);
        int twoStarCount = countByRating(reviews, 2);
        int threeStarCount = countByRating(reviews, 3);
        int fourStarCount = countByRating(reviews, 4);
        int fiveStarCount = countByRating(reviews, 5);

        int totalReviews = reviews.size();
        int verifiedPurchaseCount = (int) reviews.stream().filter(Review::isVerifiedPurchase).count();
        int positiveReviewCount = (int) reviews.stream().filter(review -> "POSITIVE".equals(review.getSentimentLabel())).count();
        int neutralReviewCount = (int) reviews.stream().filter(review -> "NEUTRAL".equals(review.getSentimentLabel())).count();
        int negativeReviewCount = (int) reviews.stream().filter(review -> "NEGATIVE".equals(review.getSentimentLabel())).count();

        BigDecimal averageRating = BigDecimal.valueOf(
                reviews.stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0.0)
        ).setScale(2, RoundingMode.HALF_UP);

        product.setRatingAverage(averageRating);

        return ReviewAnalytics.builder()
                .product(product)
                .totalReviews(totalReviews)
                .averageRating(averageRating)
                .oneStarCount(oneStarCount)
                .twoStarCount(twoStarCount)
                .threeStarCount(threeStarCount)
                .fourStarCount(fourStarCount)
                .fiveStarCount(fiveStarCount)
                .verifiedPurchaseCount(verifiedPurchaseCount)
                .positiveReviewCount(positiveReviewCount)
                .neutralReviewCount(neutralReviewCount)
                .negativeReviewCount(negativeReviewCount)
                .latestReviewSubmittedAt(reviews.stream().map(Review::getSubmittedAt).max(OffsetDateTime::compareTo).orElse(null))
                .commonKeywordsPlaceholder(buildKeywordPlaceholder(product))
                .summaryContext(buildSummaryContext(product, averageRating, totalReviews))
                .summaryReady(totalReviews >= 5)
                .lastAggregatedAt(OffsetDateTime.now())
                .build();
    }

    private int deriveRating(int productIndex, int reviewIndex) {
        int selector = (productIndex + reviewIndex) % 10;
        if (selector <= 2) {
            return 5;
        }
        if (selector <= 5) {
            return 4;
        }
        if (selector <= 7) {
            return 3;
        }
        return selector == 8 ? 2 : 1;
    }

    private int countByRating(List<Review> reviews, int rating) {
        return (int) reviews.stream()
                .filter(review -> review.getRating() == rating)
                .count();
    }

    private String buildShortDescription(String categorySlug, String displayName) {
        return switch (categorySlug) {
            case "whey-protein" -> "High-protein powder designed for recovery, lean muscle support, and convenient daily intake.";
            case "plant-protein" -> displayName + " positioned for vegan-friendly recovery and everyday protein coverage.";
            case "creatine" -> displayName + " built for strength blocks, power output, and simple stack integration.";
            case "pre-workout" -> displayName + " aimed at focus, energy, and sharper session quality before training.";
            case "hydration" -> displayName + " for hydration support during intense sessions, heat, and endurance work.";
            case "vitamins-minerals" -> displayName + " for daily recovery, wellness support, and training consistency.";
            default -> displayName + " for general wellness and routine support.";
        };
    }

    private String buildLongDescription(String categorySlug, String displayName, String brandName) {
        return switch (categorySlug) {
            case "whey-protein" -> brandName + " " + displayName + " is positioned as a clean post-workout protein option with balanced taste, strong protein density, and manageable sugar for users comparing recovery products.";
            case "plant-protein" -> brandName + " " + displayName + " combines plant-based protein positioning with practical everyday use, making it easier to compare vegan options across digestibility, price, and macros.";
            case "creatine" -> brandName + " " + displayName + " is described as a straightforward performance-support product for users prioritizing strength progress, training quality, and simple stack compatibility.";
            case "pre-workout" -> brandName + " " + displayName + " focuses on pre-training energy, workout readiness, and formula clarity so users can compare stimulant level, sweetness, and value.";
            case "hydration" -> brandName + " " + displayName + " supports hydration and electrolyte replacement for high-sweat sessions, longer cardio blocks, and warm-weather training.";
            case "vitamins-minerals" -> brandName + " " + displayName + " is framed as an educational wellness-support product that helps users compare ingredient purpose, routine fit, and basic nutritional relevance.";
            default -> brandName + " " + displayName + " is seeded as a general catalog product for testing browse and comparison flows.";
        };
    }

    private String buildReviewTitle(String productName, int rating) {
        return switch (rating) {
            case 5 -> "One of the better options in this category";
            case 4 -> "Solid result after a few weeks";
            case 3 -> "Decent, but not my favorite";
            case 2 -> "Usable, though I expected more";
            default -> "Did not work well for my routine";
        } + " - " + productName;
    }

    private String buildReviewBody(String productName, String phrase, int rating) {
        return productName + " " + phrase + ". "
                + (rating >= 4
                ? "I would likely repurchase if the price stays similar."
                : rating == 3
                ? "I can finish the tub, but I will still compare alternatives."
                : "I would not recommend it without a discount or a specific need.");
    }

    private String buildKeywordPlaceholder(Product product) {
        List<String> keywords = new ArrayList<>();
        keywords.add(product.getCategory().getSlug());
        keywords.add(product.getBrand().getName().toLowerCase(Locale.ROOT).replace(" ", "-"));
        if (product.getProteinGrams() != null && product.getProteinGrams().compareTo(BigDecimal.ZERO) > 0) {
            keywords.add("high-protein");
        }
        if (product.getSugarGrams() != null && product.getSugarGrams().compareTo(new BigDecimal("2.00")) <= 0) {
            keywords.add("low-sugar");
        }
        if (product.getDietaryFlags().stream().anyMatch(flag -> "vegan".equals(flag.getSlug()))) {
            keywords.add("vegan");
        }
        return String.join(", ", keywords);
    }

    private String buildSummaryContext(Product product, BigDecimal averageRating, int totalReviews) {
        return product.getName()
                + " has "
                + totalReviews
                + " seeded reviews with an average rating of "
                + averageRating
                + ". Common discussion areas include taste, mixability, digestion, value, and fit for specific training goals.";
    }

    private String buildSku(String categorySlug, String brandName, int index) {
        String categoryToken = categorySlug.replace("-", "").toUpperCase(Locale.ROOT);
        String brandToken = brandName.replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ROOT);
        return categoryToken.substring(0, Math.min(categoryToken.length(), 4))
                + "-"
                + brandToken.substring(0, Math.min(brandToken.length(), 4))
                + "-"
                + String.format(Locale.ROOT, "%03d", index + 1);
    }

    private String slugify(String input) {
        return input.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private record BrandSeed(String name, String websiteUrl, String countryCode) {
    }

    private record ReferenceSeed(String slug, String name, String description) {
    }

    private record ProductSeed(
            String categorySlug,
            String displayName,
            double basePrice,
            double proteinGrams,
            double sugarGrams,
            boolean active
    ) {
    }
}
