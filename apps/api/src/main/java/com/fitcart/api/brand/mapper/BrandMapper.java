package com.fitcart.api.brand.mapper;

import com.fitcart.api.brand.domain.entity.Brand;
import com.fitcart.api.brand.dto.BrandRequest;
import com.fitcart.api.brand.dto.BrandResponse;
import org.springframework.stereotype.Component;

@Component
public class BrandMapper {

    public Brand toEntity(BrandRequest request) {
        return Brand.builder()
                .name(request.name())
                .websiteUrl(request.websiteUrl())
                .countryCode(request.countryCode())
                .build();
    }

    public BrandResponse toResponse(Brand brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getWebsiteUrl(),
                brand.getCountryCode()
        );
    }
}
