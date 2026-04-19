package com.fitcart.api.product.service;

import com.fitcart.api.common.pagination.PageResponse;
import com.fitcart.api.product.dto.CreateProductRequest;
import com.fitcart.api.product.dto.ProductDetailResponse;
import com.fitcart.api.product.dto.ProductFilterRequest;
import com.fitcart.api.product.dto.ProductSummaryResponse;

public interface ProductService {

    ProductDetailResponse createProduct(CreateProductRequest request);

    PageResponse<ProductSummaryResponse> getProducts(
            ProductFilterRequest filter,
            int page,
            int size,
            String sortBy,
            String sortDir
    );

    ProductDetailResponse getProduct(Long id);
}
