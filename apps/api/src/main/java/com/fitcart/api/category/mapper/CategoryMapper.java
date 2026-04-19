package com.fitcart.api.category.mapper;

import com.fitcart.api.category.domain.entity.Category;
import com.fitcart.api.category.dto.CategoryRequest;
import com.fitcart.api.category.dto.CategoryResponse;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequest request) {
        return Category.builder()
                .slug(request.slug())
                .name(request.name())
                .description(request.description())
                .build();
    }

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getSlug(),
                category.getName(),
                category.getDescription()
        );
    }
}
