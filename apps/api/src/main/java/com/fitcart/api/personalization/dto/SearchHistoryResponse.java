package com.fitcart.api.personalization.dto;

import java.time.OffsetDateTime;

public record SearchHistoryResponse(
        Long id,
        String userReference,
        String queryText,
        String categoryHint,
        String goal,
        OffsetDateTime searchedAt
) {
}
