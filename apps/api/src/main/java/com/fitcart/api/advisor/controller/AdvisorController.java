package com.fitcart.api.advisor.controller;

import com.fitcart.api.advisor.dto.AdvisorQueryRequest;
import com.fitcart.api.advisor.dto.AdvisorResponse;
import com.fitcart.api.advisor.service.AdvisorService;
import com.fitcart.api.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/advisor")
@RequiredArgsConstructor
@Tag(name = "Advisor", description = "AI advisor orchestration endpoints")
public class AdvisorController {

    private final AdvisorService advisorService;

    @PostMapping("/recommend")
    @Operation(summary = "Parse a user shopping query, retrieve candidates, rank them, and return a grounded recommendation")
    public ApiResponse<AdvisorResponse> recommend(@Valid @RequestBody AdvisorQueryRequest request) {
        return ApiResponse.success(advisorService.advise(request));
    }
}
