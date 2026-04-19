package com.fitcart.api.brand.repository;

import com.fitcart.api.brand.domain.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
}
