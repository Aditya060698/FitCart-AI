package com.fitcart.api.ranking.dto;

import java.util.List;

public record RankProductsResponse(
        int totalCandidates,
        int rankedCount,
        List<RankedProductResponse> products
) {
}
