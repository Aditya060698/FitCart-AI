package com.fitcart.api.autocomplete.domain;

public record AutocompleteSuggestion(
        String value,
        String normalizedValue,
        AutocompleteType type,
        long weight
) {
}
