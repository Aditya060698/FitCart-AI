package com.fitcart.api.autocomplete.domain;

import java.util.HashMap;
import java.util.Map;

public class TrieNode {

    private final Map<Character, TrieNode> children = new HashMap<>();
    private final Map<String, AutocompleteSuggestion> suggestions = new HashMap<>();

    public Map<Character, TrieNode> getChildren() {
        return children;
    }

    public Map<String, AutocompleteSuggestion> getSuggestions() {
        return suggestions;
    }
}
