package com.fitcart.api.autocomplete.service.impl;

import com.fitcart.api.autocomplete.domain.AutocompleteSuggestion;
import com.fitcart.api.autocomplete.domain.AutocompleteType;
import com.fitcart.api.autocomplete.domain.TrieAutocompleteIndex;
import com.fitcart.api.autocomplete.dto.AutocompleteResponse;
import com.fitcart.api.autocomplete.dto.AutocompleteSuggestionResponse;
import com.fitcart.api.autocomplete.service.AutocompleteService;
import com.fitcart.api.brand.repository.BrandRepository;
import com.fitcart.api.common.cache.CacheNames;
import com.fitcart.api.goal.repository.GoalRepository;
import com.fitcart.api.product.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class AutocompleteServiceImpl implements AutocompleteService {

    private static final int DEFAULT_LIMIT = 10;

    private static final List<String> DEFAULT_INGREDIENT_TERMS = List.of(
            "Whey Isolate",
            "Whey Concentrate",
            "Creatine Monohydrate",
            "Casein",
            "Pea Protein",
            "Brown Rice Protein",
            "Electrolytes",
            "Magnesium",
            "Vitamin D3",
            "Omega-3",
            "Ashwagandha"
    );

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final GoalRepository goalRepository;

    private final AtomicReference<TrieAutocompleteIndex> indexRef = new AtomicReference<>(new TrieAutocompleteIndex());

    @PostConstruct
    public void initialize() {
        rebuildIndex();
    }

    @Override
    @Cacheable(
            cacheNames = CacheNames.AUTOCOMPLETE_RESULTS,
            key = "T(com.fitcart.api.common.cache.CacheKeyBuilder).autocomplete(#query, #types, #limit)",
            unless = "#query == null || #query.isBlank()"
    )
    public AutocompleteResponse search(String query, Set<AutocompleteType> types, int limit) {
        int resolvedLimit = limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, 20);
        Set<AutocompleteType> resolvedTypes = (types == null || types.isEmpty())
                ? Set.of(AutocompleteType.values())
                : types;

        List<AutocompleteSuggestionResponse> responses = indexRef.get()
                .search(query, resolvedLimit * 3L > Integer.MAX_VALUE ? resolvedLimit : resolvedLimit * 3)
                .stream()
                .filter(suggestion -> resolvedTypes.contains(suggestion.type()))
                .limit(resolvedLimit)
                .map(suggestion -> new AutocompleteSuggestionResponse(
                        suggestion.value(),
                        suggestion.type().name().toLowerCase()
                ))
                .toList();

        return new AutocompleteResponse(query, responses);
    }

    @Override
    @CacheEvict(cacheNames = CacheNames.AUTOCOMPLETE_RESULTS, allEntries = true)
    public synchronized void rebuildIndex() {
        TrieAutocompleteIndex rebuiltIndex = new TrieAutocompleteIndex();

        productRepository.findAll()
                .forEach(product -> rebuiltIndex.insert(
                        new AutocompleteSuggestion(
                                product.getName(),
                                TrieAutocompleteIndex.normalize(product.getName()),
                                AutocompleteType.PRODUCT,
                                calculateProductWeight(product.getRatingAverage())
                        )
                ));

        brandRepository.findAll()
                .forEach(brand -> rebuiltIndex.insert(
                        new AutocompleteSuggestion(
                                brand.getName(),
                                TrieAutocompleteIndex.normalize(brand.getName()),
                                AutocompleteType.BRAND,
                                60
                        )
                ));

        goalRepository.findAll()
                .forEach(goal -> rebuiltIndex.insert(
                        new AutocompleteSuggestion(
                                goal.getName(),
                                TrieAutocompleteIndex.normalize(goal.getName()),
                                AutocompleteType.GOAL,
                                50
                        )
                ));

        DEFAULT_INGREDIENT_TERMS.forEach(term -> rebuiltIndex.insert(
                new AutocompleteSuggestion(
                        term,
                        TrieAutocompleteIndex.normalize(term),
                        AutocompleteType.INGREDIENT,
                        40
                )
        ));

        indexRef.set(rebuiltIndex);
    }

    private long calculateProductWeight(java.math.BigDecimal ratingAverage) {
        if (ratingAverage == null) {
            return 100;
        }

        return Math.round(ratingAverage.doubleValue() * 100);
    }
}
