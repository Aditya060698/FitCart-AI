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

import java.time.OffsetDateTime;

@Entity
@Table(name = "search_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryEntry extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String userReference;

    @Column(nullable = false, length = 500)
    private String queryText;

    @Column(length = 120)
    private String categoryHint;

    @Column(length = 120)
    private String goal;

    @Column(nullable = false)
    private OffsetDateTime searchedAt;
}
