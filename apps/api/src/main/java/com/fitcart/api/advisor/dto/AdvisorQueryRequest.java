package com.fitcart.api.advisor.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdvisorQueryRequest(
        @NotBlank String query,
        @Min(1) @Max(10) Integer topK,
        @Size(max = 120) String userReference
) {
}
