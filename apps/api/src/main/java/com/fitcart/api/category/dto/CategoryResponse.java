package com.fitcart.api.category.dto;

public record CategoryResponse(
        Long id,
        String slug,
        String name,
        String description
) {
}
