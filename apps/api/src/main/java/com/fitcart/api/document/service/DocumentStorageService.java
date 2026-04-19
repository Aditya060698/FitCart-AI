package com.fitcart.api.document.service;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentStorageService {

    String store(String userReference, MultipartFile file);
}
