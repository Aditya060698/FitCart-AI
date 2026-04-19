package com.fitcart.api.document.service.impl;

import com.fitcart.api.document.controller.DocumentController;
import com.fitcart.api.document.dto.DocumentUploadResponse;
import com.fitcart.api.document.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
class DocumentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @Test
    void uploadDocumentShouldReturnWrappedResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "report.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "sample".getBytes()
        );

        when(documentService.uploadDocument(eq("user-101"), eq("BLOOD_REPORT"), eq(file)))
                .thenReturn(new DocumentUploadResponse(
                        11L,
                        "user-101",
                        "report.pdf",
                        MediaType.APPLICATION_PDF_VALUE,
                        6L,
                        "uploads/documents/user-101/report.pdf",
                        "BLOOD_REPORT",
                        "EXTRACTION_PENDING",
                        "Text extraction pending.",
                        null,
                        OffsetDateTime.parse("2026-04-19T12:00:00Z"),
                        null
                ));

        mockMvc.perform(multipart("/api/v1/documents")
                        .file(file)
                        .param("userReference", "user-101")
                        .param("documentType", "BLOOD_REPORT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(11))
                .andExpect(jsonPath("$.data.processingStatus").value("EXTRACTION_PENDING"));
    }
}
