package com.fitcart.api.brand.dto;

public record BrandResponse(
        Long id,
        String name,
        String websiteUrl,
        String countryCode
) {
}
