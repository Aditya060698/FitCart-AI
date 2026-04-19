package com.fitcart.api.brand.service;

import com.fitcart.api.brand.dto.BrandRequest;
import com.fitcart.api.brand.dto.BrandResponse;

import java.util.List;

public interface BrandService {

    BrandResponse createBrand(BrandRequest request);

    List<BrandResponse> getBrands();

    BrandResponse getBrand(Long id);
}
