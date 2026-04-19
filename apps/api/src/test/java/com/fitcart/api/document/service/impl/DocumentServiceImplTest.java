package com.fitcart.api.document.service.impl;

import com.fitcart.api.document.domain.entity.UploadedDocument;
import com.fitcart.api.document.dto.DocumentStatusUpdateRequest;
import com.fitcart.api.document.dto.DocumentUploadResponse;
import com.fitcart.api.document.repository.UploadedDocumentRepository;
import com.fitcart.api.document.service.DocumentStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private UploadedDocumentRepository uploadedDocumentRepository;

    @Mock
    private DocumentStorageService documentStorageService;

    private DocumentServiceImpl documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentServiceImpl(uploadedDocumentRepository, documentStorageService);
    }

    @Test
    void uploadDocumentShouldPersistMetadataAndPlaceholderText() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "report.pdf",
                "application/pdf",
                "sample".getBytes()
        );

        when(documentStorageService.store("user-101", file)).thenReturn("uploads/documents/user-101/report.pdf");
        when(uploadedDocumentRepository.save(any(UploadedDocument.class))).thenAnswer(invocation -> {
            UploadedDocument document = invocation.getArgument(0);
            document.setId(10L);
            return document;
        });

        DocumentUploadResponse response = documentService.uploadDocument("user-101", "BLOOD_REPORT", file);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.userReference()).isEqualTo("user-101");
        assertThat(response.processingStatus()).isEqualTo("EXTRACTION_PENDING");
        assertThat(response.extractedText()).isEqualTo("Text extraction pending.");
    }

    @Test
    void updateDocumentProcessingShouldUpdateStatusAndExtractedText() {
        UploadedDocument document = UploadedDocument.builder()
                .id(10L)
                .userReference("user-101")
                .fileName("report.pdf")
                .mimeType("application/pdf")
                .fileSizeBytes(1024L)
                .storagePath("uploads/documents/user-101/report.pdf")
                .documentType("BLOOD_REPORT")
                .processingStatus("EXTRACTION_PENDING")
                .extractedText("Text extraction pending.")
                .uploadedAt(OffsetDateTime.now())
                .build();

        when(uploadedDocumentRepository.findById(10L)).thenReturn(Optional.of(document));
        when(uploadedDocumentRepository.save(any(UploadedDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentUploadResponse response = documentService.updateDocumentProcessing(
                10L,
                new DocumentStatusUpdateRequest(
                        "AI_ANALYSIS_PENDING",
                        "Hemoglobin and vitamin D values extracted.",
                        null
                )
        );

        assertThat(response.processingStatus()).isEqualTo("AI_ANALYSIS_PENDING");
        assertThat(response.extractedText()).contains("Hemoglobin");
        assertThat(response.processedAt()).isNotNull();
    }

    @Test
    void uploadDocumentShouldRejectUnsupportedMimeType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "report.exe",
                "application/octet-stream",
                "sample".getBytes()
        );

        assertThatThrownBy(() -> documentService.uploadDocument("user-101", "BLOOD_REPORT", file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only PDF, PNG, JPEG, and WEBP uploads are supported");
    }
}
