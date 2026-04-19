package com.fitcart.api.ranking.controller;

import com.fitcart.api.common.api.ApiResponse;
import com.fitcart.api.ranking.dto.RankProductsRequest;
import com.fitcart.api.ranking.dto.RankProductsResponse;
import com.fitcart.api.ranking.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ranking")
@RequiredArgsConstructor
@Tag(name = "Ranking", description = "Product ranking endpoints")
public class RankingController {

    private final RankingService rankingService;

    @PostMapping("/products")
    @Operation(summary = "Rank retrieved product candidates using weighted feature scoring")
    public ApiResponse<RankProductsResponse> rankProducts(@Valid @RequestBody RankProductsRequest request) {
        return ApiResponse.success(rankingService.rankProducts(request));
    }
}
