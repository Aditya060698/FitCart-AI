package com.fitcart.api.document.repository;

import com.fitcart.api.document.domain.entity.UploadedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadedDocumentRepository extends JpaRepository<UploadedDocument, Long> {

    List<UploadedDocument> findByUserReferenceOrderByUploadedAtDesc(String userReference);
}
