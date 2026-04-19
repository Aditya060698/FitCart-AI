package com.fitcart.api.brand.service.impl;

import com.fitcart.api.brand.domain.entity.Brand;
import com.fitcart.api.brand.dto.BrandRequest;
import com.fitcart.api.brand.dto.BrandResponse;
import com.fitcart.api.brand.mapper.BrandMapper;
import com.fitcart.api.brand.repository.BrandRepository;
import com.fitcart.api.brand.service.BrandService;
import com.fitcart.api.autocomplete.service.AutocompleteService;
import com.fitcart.api.common.cache.CacheNames;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;
    private final AutocompleteService autocompleteService;

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.AUTOCOMPLETE_RESULTS, allEntries = true)
    public BrandResponse createBrand(BrandRequest request) {
        Brand saved = brandRepository.save(brandMapper.toEntity(request));
        autocompleteService.rebuildIndex();
        return brandMapper.toResponse(saved);
    }

    @Override
    public List<BrandResponse> getBrands() {
        return brandRepository.findAll()
                .stream()
                .map(brandMapper::toResponse)
                .toList();
    }

    @Override
    public BrandResponse getBrand(Long id) {
        return brandMapper.toResponse(findBrand(id));
    }

    private Brand findBrand(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found for id " + id));
    }
}
