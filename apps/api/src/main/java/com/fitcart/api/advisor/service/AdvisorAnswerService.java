package com.fitcart.api.advisor.service;

import com.fitcart.api.advisor.dto.AdvisorProductRecommendationResponse;
import com.fitcart.api.advisor.dto.ParsedAdvisorIntentResponse;

import java.util.List;

public interface AdvisorAnswerService {

    String generateAnswer(ParsedAdvisorIntentResponse intent, List<AdvisorProductRecommendationResponse> recommendations);
}
