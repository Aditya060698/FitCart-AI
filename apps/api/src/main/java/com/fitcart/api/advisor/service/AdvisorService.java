package com.fitcart.api.advisor.service;

import com.fitcart.api.advisor.dto.AdvisorQueryRequest;
import com.fitcart.api.advisor.dto.AdvisorResponse;

public interface AdvisorService {

    AdvisorResponse advise(AdvisorQueryRequest request);
}
