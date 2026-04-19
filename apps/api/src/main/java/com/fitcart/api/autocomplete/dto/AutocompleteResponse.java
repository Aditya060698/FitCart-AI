package com.fitcart.api.autocomplete.dto;

import java.util.List;

public record AutocompleteResponse(
        String query,
        List<AutocompleteSuggestionResponse> suggestions
) {
}
