package com.fitcart.api.personalization.repository;

import com.fitcart.api.personalization.domain.entity.SearchHistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchHistoryEntryRepository extends JpaRepository<SearchHistoryEntry, Long> {

    List<SearchHistoryEntry> findTop20ByUserReferenceOrderBySearchedAtDesc(String userReference);
}
