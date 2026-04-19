package com.fitcart.api.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DocumentStatusUpdateRequest(
        @NotBlank String processingStatus,
        @Size(max = 20000) String extractedText,
        @Size(max = 1000) String processingErrorMessage
) {
}
