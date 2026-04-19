package com.fitcart.api.dietaryflag.repository;

import com.fitcart.api.dietaryflag.domain.entity.DietaryFlag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DietaryFlagRepository extends JpaRepository<DietaryFlag, Long> {
}
