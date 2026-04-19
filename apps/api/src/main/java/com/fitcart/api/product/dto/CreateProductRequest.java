package com.fitcart.api.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

public record CreateProductRequest(
        @NotNull
        Long brandId,

        @NotNull
        Long categoryId,

        @Size(max = 100)
        String sku,

        @NotBlank
        @Size(max = 200)
        String name,

        @NotBlank
        @Size(max = 220)
        String slug,

        @Size(max = 500)
        String shortDescription,

        String description,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal price,

        @NotBlank
        @Size(max = 10)
        String currencyCode,

        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal proteinGrams,

        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal sugarGrams,

        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal ratingAverage,

        Set<Long> goalIds,

        Set<Long> dietaryFlagIds
) {
}
