package com.fitcart.api.advisor.service;

import com.fitcart.api.advisor.dto.ParsedAdvisorIntentResponse;

public interface AdvisorQueryParser {

    ParsedAdvisorIntentResponse parse(String query);
}
