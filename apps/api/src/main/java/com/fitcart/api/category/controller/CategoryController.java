package com.fitcart.api.category.controller;

import com.fitcart.api.category.dto.CategoryRequest;
import com.fitcart.api.category.dto.CategoryResponse;
import com.fitcart.api.category.service.CategoryService;
import com.fitcart.api.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a category")
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ApiResponse.success(categoryService.createCategory(request));
    }

    @GetMapping
    @Operation(summary = "Get all categories")
    public ApiResponse<List<CategoryResponse>> getCategories() {
        return ApiResponse.success(categoryService.getCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by id")
    public ApiResponse<CategoryResponse> getCategory(@PathVariable Long id) {
        return ApiResponse.success(categoryService.getCategory(id));
    }
}
