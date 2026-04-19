package com.fitcart.api.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank
        @Size(max = 80)
        String slug,

        @NotBlank
        @Size(max = 120)
        String name,

        String description
) {
}
