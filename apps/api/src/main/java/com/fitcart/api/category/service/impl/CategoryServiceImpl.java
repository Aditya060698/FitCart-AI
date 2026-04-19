package com.fitcart.api.category.service.impl;

import com.fitcart.api.category.domain.entity.Category;
import com.fitcart.api.category.dto.CategoryRequest;
import com.fitcart.api.category.dto.CategoryResponse;
import com.fitcart.api.category.mapper.CategoryMapper;
import com.fitcart.api.category.repository.CategoryRepository;
import com.fitcart.api.category.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Category saved = categoryRepository.save(categoryMapper.toEntity(request));
        return categoryMapper.toResponse(saved);
    }

    @Override
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse getCategory(Long id) {
        return categoryMapper.toResponse(findCategory(id));
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found for id " + id));
    }
}
