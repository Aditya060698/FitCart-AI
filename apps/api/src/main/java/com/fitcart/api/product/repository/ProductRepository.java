package com.fitcart.api.product.repository;

import com.fitcart.api.product.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Override
    @EntityGraph(attributePaths = {"brand", "category"})
    Page<Product> findAll(org.springframework.data.jpa.domain.Specification<Product> spec, Pageable pageable);

    @Query("""
            select p from Product p
            join fetch p.brand
            join fetch p.category
            left join fetch p.goals
            left join fetch p.dietaryFlags
            where p.id = :id
            """)
    Optional<Product> findDetailById(@Param("id") Long id);
}
