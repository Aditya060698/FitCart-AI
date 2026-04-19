package com.fitcart.api.personalization.service;

import com.fitcart.api.personalization.dto.*;

import java.util.List;

public interface PersonalizationService {

    UserPreferenceProfileResponse upsertProfile(UserPreferenceProfileRequest request);

    UserPreferenceProfileResponse getProfile(String userReference);

    SavedProductResponse saveProduct(SaveProductRequest request);

    void removeSavedProduct(String userReference, Long productId);

    List<SavedProductResponse> getSavedProducts(String userReference);

    SearchHistoryResponse recordSearchHistory(SearchHistoryRequest request);

    List<SearchHistoryResponse> getSearchHistory(String userReference);

    PersonalizationContext getContext(String userReference);
}
