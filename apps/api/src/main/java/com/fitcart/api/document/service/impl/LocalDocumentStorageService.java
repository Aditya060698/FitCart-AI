package com.fitcart.api.document.service.impl;

import com.fitcart.api.document.service.DocumentStorageService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
public class LocalDocumentStorageService implements DocumentStorageService {

    private final Path storageRoot;

    public LocalDocumentStorageService(@Value("${fitcart.documents.storage-root:uploads/documents}") String storageRoot) {
        this.storageRoot = Path.of(storageRoot).toAbsolutePath().normalize();
    }

    @Override
    public String store(String userReference, MultipartFile file) {
        try {
            Files.createDirectories(storageRoot);

            String safeUserReference = sanitizeSegment(userReference);
            Path userDirectory = storageRoot.resolve(safeUserReference);
            Files.createDirectories(userDirectory);

            String originalFileName = file.getOriginalFilename() == null ? "document" : file.getOriginalFilename();
            String storedFileName = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ROOT).format(OffsetDateTime.now())
                    + "-"
                    + UUID.randomUUID().toString().substring(0, 8)
                    + "-"
                    + sanitizeSegment(originalFileName);

            Path targetPath = userDirectory.resolve(storedFileName).normalize();
            if (!targetPath.startsWith(storageRoot)) {
                throw new EntityNotFoundException("Invalid storage path");
            }

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath.toString();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to store uploaded document", exception);
        }
    }

    private String sanitizeSegment(String value) {
        return value
                .replaceAll("[^a-zA-Z0-9._-]", "-")
                .replaceAll("-{2,}", "-");
    }
}
