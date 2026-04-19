package com.fitcart.api.brand.controller;

import com.fitcart.api.brand.dto.BrandRequest;
import com.fitcart.api.brand.dto.BrandResponse;
import com.fitcart.api.brand.service.BrandService;
import com.fitcart.api.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
@Tag(name = "Brands", description = "Brand management endpoints")
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a brand")
    public ApiResponse<BrandResponse> createBrand(@Valid @RequestBody BrandRequest request) {
        return ApiResponse.success(brandService.createBrand(request));
    }

    @GetMapping
    @Operation(summary = "Get all brands")
    public ApiResponse<List<BrandResponse>> getBrands() {
        return ApiResponse.success(brandService.getBrands());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get brand by id")
    public ApiResponse<BrandResponse> getBrand(@PathVariable Long id) {
        return ApiResponse.success(brandService.getBrand(id));
    }
}
