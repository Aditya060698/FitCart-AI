package com.fitcart.api.advisor.service;

import com.fitcart.api.advisor.dto.ParsedAdvisorIntentResponse;
import com.fitcart.api.ranking.dto.RankingCandidateRequest;

import java.util.List;

public interface AdvisorRetrievalService {

    List<RankingCandidateRequest> retrieveCandidates(ParsedAdvisorIntentResponse intent, int topK);
}
