package com.fitcart.api.personalization.repository;

import com.fitcart.api.personalization.domain.entity.UserPreferenceProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPreferenceProfileRepository extends JpaRepository<UserPreferenceProfile, Long> {

    Optional<UserPreferenceProfile> findByUserReference(String userReference);
}
