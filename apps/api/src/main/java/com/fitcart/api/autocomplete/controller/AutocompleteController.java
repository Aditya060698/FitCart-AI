package com.fitcart.api.autocomplete.controller;

import com.fitcart.api.autocomplete.domain.AutocompleteType;
import com.fitcart.api.autocomplete.dto.AutocompleteResponse;
import com.fitcart.api.autocomplete.service.AutocompleteService;
import com.fitcart.api.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Validated
@RequestMapping("/api/v1/autocomplete")
@RequiredArgsConstructor
@Tag(name = "Autocomplete", description = "Trie-based autocomplete endpoints")
public class AutocompleteController {

    private final AutocompleteService autocompleteService;

    @GetMapping
    @Operation(summary = "Get autocomplete suggestions for catalog search")
    public ApiResponse<AutocompleteResponse> autocomplete(
            @RequestParam String query,
            @RequestParam(required = false) Set<String> types,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit
    ) {
        Set<AutocompleteType> resolvedTypes = parseTypes(types);
        return ApiResponse.success(autocompleteService.search(query, resolvedTypes, limit));
    }

    private Set<AutocompleteType> parseTypes(Set<String> rawTypes) {
        if (rawTypes == null || rawTypes.isEmpty()) {
            return Set.of(AutocompleteType.values());
        }

        return rawTypes.stream()
                .flatMap(value -> Arrays.stream(value.split(",")))
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .filter(value -> !value.isBlank())
                .map(AutocompleteType::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
