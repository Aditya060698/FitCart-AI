package com.fitcart.api.document.dto;

import java.time.OffsetDateTime;

public record DocumentUploadResponse(
        Long id,
        String userReference,
        String fileName,
        String mimeType,
        Long fileSizeBytes,
        String storagePath,
        String documentType,
        String processingStatus,
        String extractedText,
        String processingErrorMessage,
        OffsetDateTime uploadedAt,
        OffsetDateTime processedAt
) {
}
