package com.fitcart.api.goal.repository;

import com.fitcart.api.goal.domain.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<Goal, Long> {
}
