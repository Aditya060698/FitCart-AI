package com.fitcart.api.ranking.service;

import com.fitcart.api.ranking.dto.RankProductsRequest;
import com.fitcart.api.ranking.dto.RankProductsResponse;

public interface RankingService {

    RankProductsResponse rankProducts(RankProductsRequest request);
}
