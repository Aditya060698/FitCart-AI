package com.fitcart.api.health.controller;

import com.fitcart.api.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Application health endpoints")
public class HealthController {

    @GetMapping
    @Operation(summary = "Basic application health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.success(Map.of(
                "status", "UP",
                "service", "fitcart-api"
        ));
    }
}
