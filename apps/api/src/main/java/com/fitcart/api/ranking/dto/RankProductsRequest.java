package com.fitcart.api.ranking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RankProductsRequest(
        @NotEmpty List<@Valid RankingCandidateRequest> candidates,
        @Valid RankingUserConstraints userConstraints,
        @Min(1) @Max(50) Integer topK
) {
}
