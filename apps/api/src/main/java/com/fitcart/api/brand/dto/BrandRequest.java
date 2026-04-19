package com.fitcart.api.brand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BrandRequest(
        @NotBlank
        @Size(max = 150)
        String name,

        @Size(max = 255)
        String websiteUrl,

        @Size(max = 10)
        String countryCode
) {
}
