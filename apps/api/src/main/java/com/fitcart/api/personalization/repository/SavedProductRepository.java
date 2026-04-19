package com.fitcart.api.personalization.repository;

import com.fitcart.api.personalization.domain.entity.SavedProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedProductRepository extends JpaRepository<SavedProduct, Long> {

    List<SavedProduct> findByUserReferenceOrderByCreatedAtDesc(String userReference);

    Optional<SavedProduct> findByUserReferenceAndProductId(String userReference, Long productId);
}
