package com.fitcart.api.category.service;

import com.fitcart.api.category.dto.CategoryRequest;
import com.fitcart.api.category.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    List<CategoryResponse> getCategories();

    CategoryResponse getCategory(Long id);
}
