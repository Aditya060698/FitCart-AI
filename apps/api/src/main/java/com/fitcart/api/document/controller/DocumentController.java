package com.fitcart.api.document.controller;

import com.fitcart.api.common.api.ApiResponse;
import com.fitcart.api.document.dto.DocumentStatusUpdateRequest;
import com.fitcart.api.document.dto.DocumentUploadResponse;
import com.fitcart.api.document.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Document upload and metadata endpoints")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "Upload a PDF or image document and store metadata for async processing")
    public ApiResponse<DocumentUploadResponse> uploadDocument(
            @RequestParam @NotBlank String userReference,
            @RequestParam(required = false) String documentType,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.success(documentService.uploadDocument(userReference, documentType, file));
    }

    @GetMapping("/{documentId}")
    @Operation(summary = "Get uploaded document metadata")
    public ApiResponse<DocumentUploadResponse> getDocument(@PathVariable Long documentId) {
        return ApiResponse.success(documentService.getDocument(documentId));
    }

    @GetMapping
    @Operation(summary = "List documents for a user reference")
    public ApiResponse<List<DocumentUploadResponse>> getDocumentsByUser(@RequestParam @NotBlank String userReference) {
        return ApiResponse.success(documentService.getDocumentsByUser(userReference));
    }

    @PatchMapping("/{documentId}/processing")
    @Operation(summary = "Update processing status and extracted text placeholder for async AI workflow")
    public ApiResponse<DocumentUploadResponse> updateProcessing(
            @PathVariable Long documentId,
            @Valid @org.springframework.web.bind.annotation.RequestBody DocumentStatusUpdateRequest request
    ) {
        return ApiResponse.success(documentService.updateDocumentProcessing(documentId, request));
    }
}
