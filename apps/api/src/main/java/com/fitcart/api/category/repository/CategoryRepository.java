package com.fitcart.api.category.repository;

import com.fitcart.api.category.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
