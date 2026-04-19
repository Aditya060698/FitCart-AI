package com.fitcart.api.personalization.controller;

import com.fitcart.api.common.api.ApiResponse;
import com.fitcart.api.personalization.dto.SaveProductRequest;
import com.fitcart.api.personalization.dto.SavedProductResponse;
import com.fitcart.api.personalization.dto.SearchHistoryRequest;
import com.fitcart.api.personalization.dto.SearchHistoryResponse;
import com.fitcart.api.personalization.dto.UserPreferenceProfileRequest;
import com.fitcart.api.personalization.dto.UserPreferenceProfileResponse;
import com.fitcart.api.personalization.service.PersonalizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/personalization")
@RequiredArgsConstructor
@Tag(name = "Personalization", description = "Lightweight personalization endpoints")
public class PersonalizationController {

    private final PersonalizationService personalizationService;

    @GetMapping("/profile")
    @Operation(summary = "Get user preference profile")
    public ApiResponse<UserPreferenceProfileResponse> getProfile(@RequestParam @NotBlank String userReference) {
        return ApiResponse.success(personalizationService.getProfile(userReference));
    }

    @PutMapping("/profile")
    @Operation(summary = "Create or update user preference profile")
    public ApiResponse<UserPreferenceProfileResponse> upsertProfile(@Valid @RequestBody UserPreferenceProfileRequest request) {
        return ApiResponse.success(personalizationService.upsertProfile(request));
    }

    @GetMapping("/saved-products")
    @Operation(summary = "Get saved products for a user")
    public ApiResponse<List<SavedProductResponse>> getSavedProducts(@RequestParam @NotBlank String userReference) {
        return ApiResponse.success(personalizationService.getSavedProducts(userReference));
    }

    @PostMapping("/saved-products")
    @Operation(summary = "Save a product for a user")
    public ApiResponse<SavedProductResponse> saveProduct(@Valid @RequestBody SaveProductRequest request) {
        return ApiResponse.success(personalizationService.saveProduct(request));
    }

    @DeleteMapping("/saved-products/{productId}")
    @Operation(summary = "Remove a saved product for a user")
    public ApiResponse<Void> removeSavedProduct(
            @PathVariable Long productId,
            @RequestParam @NotBlank String userReference
    ) {
        personalizationService.removeSavedProduct(userReference, productId);
        return ApiResponse.success(null);
    }

    @GetMapping("/search-history")
    @Operation(summary = "Get recent search history for a user")
    public ApiResponse<List<SearchHistoryResponse>> getSearchHistory(@RequestParam @NotBlank String userReference) {
        return ApiResponse.success(personalizationService.getSearchHistory(userReference));
    }

    @PostMapping("/search-history")
    @Operation(summary = "Record a search or advisor query")
    public ApiResponse<SearchHistoryResponse> recordSearchHistory(@Valid @RequestBody SearchHistoryRequest request) {
        return ApiResponse.success(personalizationService.recordSearchHistory(request));
    }
}
