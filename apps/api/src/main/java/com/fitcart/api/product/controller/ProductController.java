package com.fitcart.api.product.controller;

import com.fitcart.api.common.api.ApiResponse;
import com.fitcart.api.common.pagination.PageResponse;
import com.fitcart.api.product.dto.CreateProductRequest;
import com.fitcart.api.product.dto.ProductDetailResponse;
import com.fitcart.api.product.dto.ProductFilterRequest;
import com.fitcart.api.product.dto.ProductSummaryResponse;
import com.fitcart.api.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Set;

@RestController
@Validated
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog endpoints")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a product")
    public ApiResponse<ProductDetailResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ApiResponse.success(productService.createProduct(request));
    }

    @GetMapping
    @Operation(summary = "Get paginated products with filter skeleton")
    public ApiResponse<PageResponse<ProductSummaryResponse>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long goalId,
            @RequestParam(required = false) BigDecimal minProteinGrams,
            @RequestParam(required = false) BigDecimal maxProteinGrams,
            @RequestParam(required = false) BigDecimal minSugarGrams,
            @RequestParam(required = false) BigDecimal maxSugarGrams,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) Set<Long> dietaryFlagIds,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        ProductFilterRequest filter = new ProductFilterRequest(
                categoryId,
                brandId,
                search,
                active,
                minPrice,
                maxPrice,
                goalId,
                minProteinGrams,
                maxProteinGrams,
                minSugarGrams,
                maxSugarGrams,
                minRating,
                dietaryFlagIds
        );
        return ApiResponse.success(productService.getProducts(filter, page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product detail")
    public ApiResponse<ProductDetailResponse> getProduct(@PathVariable Long id) {
        return ApiResponse.success(productService.getProduct(id));
    }
}
