package com.fitcart.api.personalization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveProductRequest(
        @NotBlank String userReference,
        @NotNull Long productId
) {
}
