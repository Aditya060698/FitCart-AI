package com.fitcart.api.personalization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SearchHistoryRequest(
        @NotBlank String userReference,
        @NotBlank @Size(max = 500) String queryText,
        @Size(max = 120) String categoryHint,
        @Size(max = 120) String goal
) {
}
