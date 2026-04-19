package com.fitcart.api.document.service;

import com.fitcart.api.document.dto.DocumentStatusUpdateRequest;
import com.fitcart.api.document.dto.DocumentUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    DocumentUploadResponse uploadDocument(String userReference, String documentType, MultipartFile file);

    DocumentUploadResponse getDocument(Long documentId);

    List<DocumentUploadResponse> getDocumentsByUser(String userReference);

    DocumentUploadResponse updateDocumentProcessing(Long documentId, DocumentStatusUpdateRequest request);
}
