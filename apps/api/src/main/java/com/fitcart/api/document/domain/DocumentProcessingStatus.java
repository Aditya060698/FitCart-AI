package com.fitcart.api.document.domain;

public enum DocumentProcessingStatus {
    UPLOADED,
    EXTRACTION_PENDING,
    EXTRACTION_COMPLETE,
    AI_ANALYSIS_PENDING,
    COMPLETED,
    FAILED
}
