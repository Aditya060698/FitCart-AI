package com.fitcart.api.personalization.service.impl;

import com.fitcart.api.dietaryflag.domain.entity.DietaryFlag;
import com.fitcart.api.personalization.domain.entity.SavedProduct;
import com.fitcart.api.personalization.domain.entity.SearchHistoryEntry;
import com.fitcart.api.personalization.domain.entity.UserPreferenceProfile;
import com.fitcart.api.personalization.dto.PersonalizationContext;
import com.fitcart.api.personalization.dto.SaveProductRequest;
import com.fitcart.api.personalization.dto.SavedProductResponse;
import com.fitcart.api.personalization.dto.SearchHistoryRequest;
import com.fitcart.api.personalization.dto.SearchHistoryResponse;
import com.fitcart.api.personalization.dto.UserPreferenceProfileRequest;
import com.fitcart.api.personalization.dto.UserPreferenceProfileResponse;
import com.fitcart.api.personalization.repository.SavedProductRepository;
import com.fitcart.api.personalization.repository.SearchHistoryEntryRepository;
import com.fitcart.api.personalization.repository.UserPreferenceProfileRepository;
import com.fitcart.api.personalization.service.PersonalizationService;
import com.fitcart.api.product.domain.entity.Product;
import com.fitcart.api.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalizationServiceImpl implements PersonalizationService {

    private final UserPreferenceProfileRepository profileRepository;
    private final SavedProductRepository savedProductRepository;
    private final SearchHistoryEntryRepository searchHistoryEntryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public UserPreferenceProfileResponse upsertProfile(UserPreferenceProfileRequest request) {
        UserPreferenceProfile profile = profileRepository.findByUserReference(request.userReference().trim())
                .orElseGet(() -> UserPreferenceProfile.builder().userReference(request.userReference().trim()).build());

        profile.setMinBudget(request.minBudget());
        profile.setMaxBudget(request.maxBudget());
        profile.setPrimaryGoal(request.primaryGoal());
        profile.setDietaryPreferences(join(request.dietaryPreferences()));

        return toProfileResponse(profileRepository.save(profile));
    }

    @Override
    public UserPreferenceProfileResponse getProfile(String userReference) {
        return profileRepository.findByUserReference(userReference.trim())
                .map(this::toProfileResponse)
                .orElse(new UserPreferenceProfileResponse(userReference.trim(), null, null, null, Set.of()));
    }

    @Override
    @Transactional
    public SavedProductResponse saveProduct(SaveProductRequest request) {
        Optional<SavedProduct> existing = savedProductRepository.findByUserReferenceAndProductId(
                request.userReference().trim(),
                request.productId()
        );
        if (existing.isPresent()) {
            return toSavedProductResponse(existing.get());
        }

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found for id " + request.productId()));

        SavedProduct savedProduct = savedProductRepository.save(SavedProduct.builder()
                .userReference(request.userReference().trim())
                .product(product)
                .build());

        return toSavedProductResponse(savedProduct);
    }

    @Override
    @Transactional
    public void removeSavedProduct(String userReference, Long productId) {
        savedProductRepository.findByUserReferenceAndProductId(userReference.trim(), productId)
                .ifPresent(savedProductRepository::delete);
    }

    @Override
    public List<SavedProductResponse> getSavedProducts(String userReference) {
        return savedProductRepository.findByUserReferenceOrderByCreatedAtDesc(userReference.trim()).stream()
                .map(this::toSavedProductResponse)
                .toList();
    }

    @Override
    @Transactional
    public SearchHistoryResponse recordSearchHistory(SearchHistoryRequest request) {
        SearchHistoryEntry entry = searchHistoryEntryRepository.save(SearchHistoryEntry.builder()
                .userReference(request.userReference().trim())
                .queryText(request.queryText().trim())
                .categoryHint(request.categoryHint())
                .goal(request.goal())
                .searchedAt(OffsetDateTime.now())
                .build());
        return toSearchHistoryResponse(entry);
    }

    @Override
    public List<SearchHistoryResponse> getSearchHistory(String userReference) {
        return searchHistoryEntryRepository.findTop20ByUserReferenceOrderBySearchedAtDesc(userReference.trim()).stream()
                .map(this::toSearchHistoryResponse)
                .toList();
    }

    @Override
    public PersonalizationContext getContext(String userReference) {
        if (userReference == null || userReference.isBlank()) {
            return new PersonalizationContext(null, null, Set.of(), Set.of(), Set.of(), Set.of(), Set.of());
        }

        UserPreferenceProfile profile = profileRepository.findByUserReference(userReference.trim()).orElse(null);
        List<SavedProduct> savedProducts = savedProductRepository.findByUserReferenceOrderByCreatedAtDesc(userReference.trim());
        List<SearchHistoryEntry> searchHistory = searchHistoryEntryRepository.findTop20ByUserReferenceOrderBySearchedAtDesc(userReference.trim());

        Set<String> preferredBrands = savedProducts.stream()
                .map(savedProduct -> savedProduct.getProduct().getBrand().getName())
                .map(this::normalize)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> preferredCategories = savedProducts.stream()
                .map(savedProduct -> savedProduct.getProduct().getCategory().getName())
                .map(this::normalize)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> recentQueryTopics = searchHistory.stream()
                .flatMap(entry -> tokenize(entry.getQueryText()).stream())
                .filter(token -> token.length() > 3)
                .limit(12)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<Long> savedProductIds = savedProducts.stream()
                .map(savedProduct -> savedProduct.getProduct().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new PersonalizationContext(
                profile == null ? null : profile.getMinBudget(),
                profile == null ? null : profile.getMaxBudget(),
                preferredBrands,
                preferredCategories,
                split(profile == null ? null : profile.getDietaryPreferences()),
                recentQueryTopics,
                savedProductIds
        );
    }

    private UserPreferenceProfileResponse toProfileResponse(UserPreferenceProfile profile) {
        return new UserPreferenceProfileResponse(
                profile.getUserReference(),
                profile.getMinBudget(),
                profile.getMaxBudget(),
                profile.getPrimaryGoal(),
                split(profile.getDietaryPreferences())
        );
    }

    private SavedProductResponse toSavedProductResponse(SavedProduct savedProduct) {
        Product product = savedProduct.getProduct();
        Set<String> dietaryFlags = product.getDietaryFlags().stream()
                .map(DietaryFlag::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new SavedProductResponse(
                savedProduct.getId(),
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getCategory().getName(),
                product.getBrand().getName(),
                product.getPrice(),
                product.getCurrencyCode(),
                product.getProteinGrams(),
                product.getSugarGrams(),
                product.getRatingAverage(),
                dietaryFlags,
                savedProduct.getCreatedAt()
        );
    }

    private SearchHistoryResponse toSearchHistoryResponse(SearchHistoryEntry entry) {
        return new SearchHistoryResponse(
                entry.getId(),
                entry.getUserReference(),
                entry.getQueryText(),
                entry.getCategoryHint(),
                entry.getGoal(),
                entry.getSearchedAt()
        );
    }

    private String join(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream()
                .map(this::normalize)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(","));
    }

    private Set<String> split(String values) {
        if (values == null || values.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(values.split(","))
                .map(this::normalize)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> tokenize(String value) {
        return Arrays.stream(normalize(value).split("\\s+"))
                .filter(token -> !token.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
