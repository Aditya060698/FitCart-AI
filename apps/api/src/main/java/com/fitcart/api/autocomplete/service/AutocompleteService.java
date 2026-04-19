package com.fitcart.api.autocomplete.service;

import com.fitcart.api.autocomplete.domain.AutocompleteType;
import com.fitcart.api.autocomplete.dto.AutocompleteResponse;

import java.util.Set;

public interface AutocompleteService {

    AutocompleteResponse search(String query, Set<AutocompleteType> types, int limit);

    void rebuildIndex();
}
