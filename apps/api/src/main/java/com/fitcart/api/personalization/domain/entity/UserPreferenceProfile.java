package com.fitcart.api.personalization.domain.entity;

import com.fitcart.api.common.domain.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "user_preference_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceProfile extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String userReference;

    @Column(precision = 12, scale = 2)
    private BigDecimal minBudget;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxBudget;

    @Column(length = 120)
    private String primaryGoal;

    @Column(length = 500)
    private String dietaryPreferences;
}
