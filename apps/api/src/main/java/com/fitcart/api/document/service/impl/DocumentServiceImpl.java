package com.fitcart.api.document.service.impl;

import com.fitcart.api.document.domain.DocumentProcessingStatus;
import com.fitcart.api.document.domain.entity.UploadedDocument;
import com.fitcart.api.document.dto.DocumentStatusUpdateRequest;
import com.fitcart.api.document.dto.DocumentUploadResponse;
import com.fitcart.api.document.repository.UploadedDocumentRepository;
import com.fitcart.api.document.service.DocumentService;
import com.fitcart.api.document.service.DocumentStorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentServiceImpl implements DocumentService {

    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/webp"
    );

    private final UploadedDocumentRepository uploadedDocumentRepository;
    private final DocumentStorageService documentStorageService;

    @Override
    @Transactional
    public DocumentUploadResponse uploadDocument(String userReference, String documentType, MultipartFile file) {
        validateUpload(userReference, file);

        String storagePath = documentStorageService.store(userReference, file);
        OffsetDateTime now = OffsetDateTime.now();

        UploadedDocument document = UploadedDocument.builder()
                .userReference(userReference.trim())
                .fileName(resolveFileName(file))
                .mimeType(file.getContentType())
                .fileSizeBytes(file.getSize())
                .storagePath(storagePath)
                .documentType(resolveDocumentType(documentType, file))
                .processingStatus(DocumentProcessingStatus.EXTRACTION_PENDING.name())
                .extractedText("Text extraction pending.")
                .processingErrorMessage(null)
                .uploadedAt(now)
                .processedAt(null)
                .build();

        return toResponse(uploadedDocumentRepository.save(document));
    }

    @Override
    public DocumentUploadResponse getDocument(Long documentId) {
        return toResponse(findDocument(documentId));
    }

    @Override
    public List<DocumentUploadResponse> getDocumentsByUser(String userReference) {
        return uploadedDocumentRepository.findByUserReferenceOrderByUploadedAtDesc(userReference.trim())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DocumentUploadResponse updateDocumentProcessing(Long documentId, DocumentStatusUpdateRequest request) {
        UploadedDocument document = findDocument(documentId);
        DocumentProcessingStatus processingStatus = DocumentProcessingStatus.valueOf(request.processingStatus().trim().toUpperCase());

        document.setProcessingStatus(processingStatus.name());
        document.setExtractedText(request.extractedText() == null || request.extractedText().isBlank()
                ? document.getExtractedText()
                : request.extractedText().trim());
        document.setProcessingErrorMessage(request.processingErrorMessage());

        if (processingStatus == DocumentProcessingStatus.COMPLETED
                || processingStatus == DocumentProcessingStatus.AI_ANALYSIS_PENDING
                || processingStatus == DocumentProcessingStatus.EXTRACTION_COMPLETE
                || processingStatus == DocumentProcessingStatus.FAILED) {
            document.setProcessedAt(OffsetDateTime.now());
        }

        return toResponse(uploadedDocumentRepository.save(document));
    }

    private UploadedDocument findDocument(Long documentId) {
        return uploadedDocumentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found for id " + documentId));
    }

    private void validateUpload(String userReference, MultipartFile file) {
        if (userReference == null || userReference.isBlank()) {
            throw new IllegalArgumentException("userReference is required");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("A non-empty file is required");
        }
        if (file.getContentType() == null || !ALLOWED_MIME_TYPES.contains(file.getContentType().toLowerCase())) {
            throw new IllegalArgumentException("Only PDF, PNG, JPEG, and WEBP uploads are supported");
        }
    }

    private String resolveDocumentType(String documentType, MultipartFile file) {
        if (documentType != null && !documentType.isBlank()) {
            return documentType.trim();
        }

        String mimeType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (mimeType.contains("pdf")) {
            return "PDF_DOCUMENT";
        }
        return "IMAGE_DOCUMENT";
    }

    private String resolveFileName(MultipartFile file) {
        return file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()
                ? "uploaded-document"
                : file.getOriginalFilename().trim();
    }

    private DocumentUploadResponse toResponse(UploadedDocument document) {
        return new DocumentUploadResponse(
                document.getId(),
                document.getUserReference(),
                document.getFileName(),
                document.getMimeType(),
                document.getFileSizeBytes(),
                document.getStoragePath(),
                document.getDocumentType(),
                document.getProcessingStatus(),
                document.getExtractedText(),
                document.getProcessingErrorMessage(),
                document.getUploadedAt(),
                document.getProcessedAt()
        );
    }
}
