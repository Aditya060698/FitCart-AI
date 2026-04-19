package com.fitcart.api.autocomplete.domain;

import java.util.Comparator;
import java.util.List;

public class TrieAutocompleteIndex {

    private final TrieNode root = new TrieNode();

    public void insert(AutocompleteSuggestion suggestion) {
        TrieNode current = root;

        for (char character : suggestion.normalizedValue().toCharArray()) {
            current = current.getChildren().computeIfAbsent(character, key -> new TrieNode());
            current.getSuggestions().merge(
                    buildSuggestionKey(suggestion),
                    suggestion,
                    this::pickHigherWeightSuggestion
            );
        }
    }

    public List<AutocompleteSuggestion> search(String prefix, int limit) {
        if (prefix == null || prefix.isBlank()) {
            return List.of();
        }

        TrieNode current = root;
        String normalizedPrefix = normalize(prefix);

        for (char character : normalizedPrefix.toCharArray()) {
            current = current.getChildren().get(character);
            if (current == null) {
                return List.of();
            }
        }

        return current.getSuggestions()
                .values()
                .stream()
                .sorted(
                        Comparator.comparingLong(AutocompleteSuggestion::weight).reversed()
                                .thenComparing(AutocompleteSuggestion::value)
                )
                .limit(limit)
                .toList();
    }

    public static String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase();
    }

    private String buildSuggestionKey(AutocompleteSuggestion suggestion) {
        return suggestion.type() + "::" + suggestion.normalizedValue();
    }

    private AutocompleteSuggestion pickHigherWeightSuggestion(
            AutocompleteSuggestion left,
            AutocompleteSuggestion right
    ) {
        return left.weight() >= right.weight() ? left : right;
    }
}
