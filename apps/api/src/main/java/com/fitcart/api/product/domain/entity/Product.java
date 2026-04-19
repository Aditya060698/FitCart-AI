package com.fitcart.api.product.domain.entity;

import com.fitcart.api.brand.domain.entity.Brand;
import com.fitcart.api.category.domain.entity.Category;
import com.fitcart.api.common.domain.BaseAuditableEntity;
import com.fitcart.api.dietaryflag.domain.entity.DietaryFlag;
import com.fitcart.api.goal.domain.entity.Goal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(length = 100, unique = true)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(length = 500)
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 10)
    private String currencyCode;

    @Column(precision = 8, scale = 2)
    private BigDecimal proteinGrams;

    @Column(precision = 8, scale = 2)
    private BigDecimal sugarGrams;

    @Column(precision = 3, scale = 2)
    private BigDecimal ratingAverage;

    @Column(nullable = false)
    private boolean active;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_goals",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "goal_id")
    )
    private Set<Goal> goals = new HashSet<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_dietary_flags",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "dietary_flag_id")
    )
    private Set<DietaryFlag> dietaryFlags = new HashSet<>();
}
